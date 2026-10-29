-- =====================================================================
-- Bibliotecnm - Script de base de datos (PostgreSQL)
-- Uso: crea la BD "bibliotecnm" y ejecuta este script dentro de ella.
-- =====================================================================

-- Para reejecutar desde cero (cuidado: borra todo)
DROP VIEW  IF EXISTS vista_libros;
DROP TABLE IF EXISTS prestamos       CASCADE;
DROP TABLE IF EXISTS ejemplares      CASCADE;
DROP TABLE IF EXISTS libros_generos  CASCADE;
DROP TABLE IF EXISTS libros_autores  CASCADE;
DROP TABLE IF EXISTS generos         CASCADE;
DROP TABLE IF EXISTS autores         CASCADE;
DROP TABLE IF EXISTS libros          CASCADE;
DROP TABLE IF EXISTS usuarios        CASCADE;

-- ---------------------------------------------------------------------
-- USUARIOS
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    correo         VARCHAR(160) NOT NULL UNIQUE,
    password_hash  TEXT         NOT NULL,
    rol            VARCHAR(20)  NOT NULL DEFAULT 'lector'
                   CHECK (rol IN ('lector', 'bibliotecario', 'admin')),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- CATALOGO
-- ---------------------------------------------------------------------
CREATE TABLE libros (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo       VARCHAR(250) NOT NULL,
    anio         SMALLINT CHECK (anio BETWEEN 1 AND 2100),
    sinopsis     TEXT,
    isbn         VARCHAR(20) UNIQUE,
    editorial    VARCHAR(150),
    url_portada  TEXT,
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE autores (
    id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE generos (
    id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL UNIQUE
);

-- Tablas puente (muchos a muchos)
CREATE TABLE libros_autores (
    libro_id  INT NOT NULL REFERENCES libros(id)  ON DELETE CASCADE,
    autor_id  INT NOT NULL REFERENCES autores(id) ON DELETE RESTRICT,
    PRIMARY KEY (libro_id, autor_id)
);

CREATE TABLE libros_generos (
    libro_id   INT NOT NULL REFERENCES libros(id)   ON DELETE CASCADE,
    genero_id  INT NOT NULL REFERENCES generos(id)  ON DELETE RESTRICT,
    PRIMARY KEY (libro_id, genero_id)
);

-- ---------------------------------------------------------------------
-- EJEMPLARES (copias fisicas de cada libro)
-- ---------------------------------------------------------------------
CREATE TABLE ejemplares (
    id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libro_id         INT NOT NULL REFERENCES libros(id) ON DELETE CASCADE,
    codigo_barras    VARCHAR(40) NOT NULL UNIQUE,
    estado           VARCHAR(20) NOT NULL DEFAULT 'activo'
                     CHECK (estado IN ('activo', 'mantenimiento', 'baja')),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- PRESTAMOS
-- Un prestamo esta "activo" mientras fecha_devolucion sea NULL.
-- ---------------------------------------------------------------------
CREATE TABLE prestamos (
    id                INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id        INT NOT NULL REFERENCES usuarios(id)   ON DELETE RESTRICT,
    ejemplar_id       INT NOT NULL REFERENCES ejemplares(id) ON DELETE RESTRICT,
    fecha_prestamo    TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_limite      DATE        NOT NULL,
    fecha_devolucion  TIMESTAMPTZ,
    CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo)
);

-- Garantia a nivel de BD: un ejemplar no puede tener dos prestamos activos
-- al mismo tiempo, aunque dos usuarios lo pidan en el mismo instante.
CREATE UNIQUE INDEX ux_prestamo_activo_por_ejemplar
    ON prestamos (ejemplar_id)
    WHERE fecha_devolucion IS NULL;

-- Indices de consulta frecuente
CREATE INDEX ix_prestamos_usuario  ON prestamos (usuario_id);
CREATE INDEX ix_ejemplares_libro   ON ejemplares (libro_id);
CREATE INDEX ix_libros_titulo      ON libros (lower(titulo));

-- ---------------------------------------------------------------------
-- VISTA: libro con autores, generos y disponibilidad
-- ---------------------------------------------------------------------
CREATE VIEW vista_libros AS
SELECT
    l.id,
    l.titulo,
    l.anio,
    l.sinopsis,
    l.isbn,
    l.editorial,
    l.url_portada,
    COALESCE((SELECT string_agg(a.nombre, ', ' ORDER BY a.nombre)
              FROM libros_autores la JOIN autores a ON a.id = la.autor_id
              WHERE la.libro_id = l.id), '') AS autores,
    COALESCE((SELECT string_agg(g.nombre, ', ' ORDER BY g.nombre)
              FROM libros_generos lg JOIN generos g ON g.id = lg.genero_id
              WHERE lg.libro_id = l.id), '') AS generos,
    (SELECT count(*) FROM ejemplares e
      WHERE e.libro_id = l.id AND e.estado = 'activo') AS ejemplares_total,
    (SELECT count(*) FROM ejemplares e
      WHERE e.libro_id = l.id AND e.estado = 'activo'
        AND NOT EXISTS (SELECT 1 FROM prestamos p
                         WHERE p.ejemplar_id = e.id
                           AND p.fecha_devolucion IS NULL)) AS ejemplares_disponibles
FROM libros l;

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================

-- Usuario de prueba: correo prueba@bibliotecnm.local, contraseña 123456
-- (password_hash = SHA-256 de "123456").
INSERT INTO usuarios (nombre, correo, password_hash, rol) VALUES
    ('Usuario de Prueba', 'prueba@bibliotecnm.local',
     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'lector');

INSERT INTO autores (nombre) VALUES
    ('Paulo Coelho'),
    ('George Orwell'),
    ('Yuval Noah Harari'),
    ('Antoine de Saint-Exupéry'),
    ('Bram Stoker');

INSERT INTO generos (nombre) VALUES
    ('Ficción'), ('Filosofía'), ('Distopía'), ('Ciencia Ficción'),
    ('Historia'), ('Antropología'), ('Literatura Infantil'),
    ('Fantasía'), ('Terror'), ('Gótico');

INSERT INTO libros (titulo, anio, isbn, editorial) VALUES
    ('El Alquimista',  1988, NULL, NULL),
    ('1984',           1949, NULL, NULL),
    ('Sapiens',        2011, NULL, NULL),
    ('El Principito',  1943, NULL, NULL),
    ('Drácula',        1897, NULL, NULL);

INSERT INTO libros_autores (libro_id, autor_id) VALUES
    (1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

INSERT INTO libros_generos (libro_id, genero_id) VALUES
    (1, 1), (1, 2),
    (2, 3), (2, 4),
    (3, 5), (3, 6),
    (4, 7), (4, 8),
    (5, 9), (5, 10);

-- Un ejemplar por libro
INSERT INTO ejemplares (libro_id, codigo_barras) VALUES
    (1, 'BIB-0001'),
    (2, 'BIB-0002'),
    (3, 'BIB-0003'),
    (4, 'BIB-0004'),
    (5, 'BIB-0005');

-- 1984 y Drácula quedan prestados (como en la app: disponible = false)
INSERT INTO prestamos (usuario_id, ejemplar_id, fecha_limite) VALUES
    (1, 2, current_date + 14),
    (1, 5, current_date + 14);
-- Migracion para soporte de prioridad por timestamp de solicitud
ALTER TABLE prestamos ADD COLUMN IF NOT EXISTS fecha_solicitud TIMESTAMPTZ;
ALTER TABLE prestamos ADD COLUMN IF NOT EXISTS fecha_recepcion TIMESTAMPTZ;

