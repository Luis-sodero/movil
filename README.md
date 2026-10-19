# API_bibliotecnm

API REST en **.NET 10** conectada a una base de datos **PostgreSQL** llamada `bibliotecnm`.

## Estructura

```
API Ivan/
├─ API_bibliotecnm.slnx              # Solución (se abre con Visual Studio / VS Code)
├─ biblotecnm.sql                    # Script de creación de la base de datos
└─ API_bibliotecnm/                  # Proyecto Web API
   ├─ Controllers/                   # Endpoints (Libros, Autores, Generos, ...)
   ├─ Data/ContextoBD.cs             # DbContext de Entity Framework Core (Npgsql)
   ├─ Models/                        # Entidades que reflejan las tablas de la BD
   ├─ Program.cs                     # Registro del DbContext y pipeline
   └─ appsettings.json               # Cadena de conexión a PostgreSQL
```

## Requisitos

- .NET SDK 10.0 (instalado)
- PostgreSQL 17 en `localhost:5432`
- Paquete NuGet `Npgsql.EntityFrameworkCore.PostgreSQL`

## Base de datos

La base `bibliotecnm` se crea y se carga con el script. Desde la carpeta del proyecto:

```powershell
$env:PGPASSWORD = "admin123"
$env:PGCLIENTENCODING = "UTF8"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -p 5432 -c "CREATE DATABASE bibliotecnm ENCODING 'UTF8' TEMPLATE template0"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -p 5432 -d bibliotecnm -v ON_ERROR_STOP=1 -f "biblotecnm.sql"
```

Tablas: `usuarios`, `libros`, `autores`, `generos`, `libros_autores`, `libros_generos`,
`ejemplares`, `prestamos` y la vista `vista_libros`.

## Cadena de conexión

`appsettings.json` solo trae un valor de ejemplo (sin contraseña real):

```json
"ConnectionStrings": {
  "ConexionPostgres": "Host=localhost;Port=5432;Database=bibliotecnm;Username=postgres;Password=CAMBIAR_CON_USER_SECRETS"
}
```

La contraseña real se guarda con **user-secrets** (fuera del repositorio). Ejecuta una vez:

```powershell
cd API_bibliotecnm
dotnet user-secrets set "ConnectionStrings:ConexionPostgres" "Host=localhost;Port=5432;Database=bibliotecnm;Username=postgres;Password=TU_PASSWORD"
```

> Los user-secrets solo se cargan en entorno **Development** (`dotnet run`). En otros
> entornos define la variable de entorno `ConnectionStrings__ConexionPostgres`.

## Ejecutar

```powershell
cd API_bibliotecnm
dotnet run
```

Swagger queda disponible en `http://localhost:5023/swagger` (entorno Development).

## Endpoints principales

| Método | Ruta                        | Descripción                                             |
| ------ | --------------------------- | ------------------------------------------------------- |
| GET    | `/api/VistaLibros`          | Libros con autores, géneros y disponibilidad (vista)    |
| GET    | `/api/Libros`               | Libros con relaciones                                   |
| GET    | `/api/Autores`              | Autores                                                 |
| GET    | `/api/Generos`              | Géneros                                                 |
| GET    | `/api/Ejemplares`           | Ejemplares (copias físicas)                             |
| GET    | `/api/Prestamos`            | Préstamos                                               |
| GET    | `/api/Usuarios`             | Usuarios                                                |

Cada controlador incluye también `GET {id}`, `POST` y `DELETE` (y `PUT` en Libros).
