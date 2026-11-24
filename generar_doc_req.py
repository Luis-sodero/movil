# -*- coding: utf-8 -*-
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt, Inches
from docx.oxml.ns import qn
from datetime import datetime
import os

doc = Document()

# Portada
title = doc.add_heading('Documento de Requerimientos y Arquitectura', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('')
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.add_run('Proyecto: Bibliotecnm').bold = True
doc.add_paragraph('Versión: 1.0')
doc.add_paragraph('Fecha de generación: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Estado del documento: En análisis / Parcial')
doc.add_paragraph('Ubicación: E:\\API')
doc.add_page_break()

# Índice (placeholder)
doc.add_heading('Tabla de Contenido', level=1)
doc.add_paragraph('[Se actualizará al abrir en Microsoft Word]')
doc.add_page_break()

# 1. Introducción y visión
doc.add_heading('1. Introducción y visión del sistema', level=1)
doc.add_heading('1.1 Propósito del documento', level=2)
doc.add_paragraph('Documentar lo existente, lo parcial y lo propuesto para completar el sistema Bibliotecnm (API REST + App Móvil Android + Web estática).')
doc.add_heading('1.2 Problema o necesidad', level=2)
doc.add_paragraph('Gestionar catálogo de libros, préstamos/devoluciones, control de ejemplares y disponibilidad.')
doc.add_heading('1.3 Objetivo general', level=2)
doc.add_paragraph('Completar la solución para gestionar préstamos de biblioteca con soporte online/offline en app móvil.')
doc.add_heading('1.4 Descripción general', level=2)
doc.add_paragraph('Solución compuesta por: API .NET (bibliotecnm_API/API_bibliotecnm) con EF Core/Postgres, App Android Kotlin/Compose (Bibliotecnm), web estática (wwwroot), BD PostgreSQL (bibliotecnm.sql).')

# 2. Alcance
doc.add_heading('2. Alcance', level=1)
doc.add_heading('2.1 Funcionalidades implementadas (comprobables)', level=2)
doc.add_paragraph('- API: Catálogo (Libros/Autores/Géneros), Ejemplares, Usuarios, Préstamos, VistaLibros, Auth básico, interfaz web estática.')
doc.add_paragraph('- Móvil: Login, catálogo con disponibilidad, solicitud de préstamo, mis préstamos, navegación, sesión en memoria.')
doc.add_paragraph('- BD: Esquema PostgreSQL con vista vista_libros, índices, restricción préstamo activo por ejemplar.')
doc.add_heading('2.2 Funcionalidades parciales', level=2)
doc.add_paragraph('- App móvil: sincronización offline (Room) implementada parcialmente para solicitudes; marcado de rechazados/avisados existe.')
doc.add_paragraph('- API Prestamos/solicitar: lógica compleja con transacciones, FOR UPDATE, manejo de concurrencia y timestamp - parcialmente verificado.')
doc.add_heading('2.3 Funcionalidades propuestas para completar', level=2)
doc.add_paragraph('- Completar flujo de devolución desde app (no evidente implementación completa).')
doc.add_paragraph('- Autenticación robusta (JWT/roles) en móvil/API según necesidades.')
doc.add_paragraph('- Notificaciones/avisos de rechazos en app.')
doc.add_paragraph('- Gestión completa desde app (renovaciones, historial).')
doc.add_heading('2.4 Exclusiones y límites', level=2)
doc.add_paragraph('- No se modificó código/configuración (solo análisis).')
doc.add_heading('2.5 Supuestos y restricciones por validar', level=2)
doc.add_paragraph('- URLs/credenciales no expuestas.')
doc.add_paragraph('- Ambiente de despliegue por confirmar.')

# 3. Usuarios y actores
doc.add_heading('3. Usuarios y actores', level=1)
table = doc.add_table(rows=1, cols=4)
hdr = table.rows[0].cells
hdr[0].text='Actor'; hdr[1].text='Tipo'; hdr[2].text='Responsabilidades'; hdr[3].text='Permisos/Estado'
rows=[('Lector (Usuario)','Humano','Solicitar préstamos, ver catálogo/mis préstamos','Lector - Implementado parcial'),
      ('Bibliotecario','Humano','Gestionar préstamos/devoluciones - por confirmar','Propuesto/Por confirmar'),
      ('Administrador','Humano','Gestión completa - por confirmar','Propuesto/Por confirmar'),
      ('App Móvil','Sistema','Consumir API','Implementado'),
      ('Web estática','Sistema','Interfaz administrativa/consulta - verificada existencia','Parcial/Por confirmar')]
for r in rows:
    c=table.add_row().cells
    for i,v in enumerate(r): c[i].text=v

# 4. Requerimientos funcionales
doc.add_heading('4. Requerimientos funcionales', level=1)
rf=[('RF-001','Login de usuario','App móvil/API','alta','parcial','Autenticar con correo/password'),
    ('RF-002','Consultar catálogo de libros','App móvil','alta','implementado','Listar desde /api/VistaLibros con disponibilidad'),
    ('RF-003','Solicitar préstamo','App móvil/API','alta','parcial','POST /api/Prestamos/solicitar con control concurrencia'),
    ('RF-004','Consultar mis préstamos','App móvil','alta','implementado','GET /api/Prestamos?usuarioId='),
    ('RF-005','Sincronización offline','App móvil','media','parcial','Guardar en Room y sincronizar al conectar'),
    ('RF-006','Devolución de préstamo','Bibliotecario/App','alta','propuesto','Por confirmar implementación completa'),
    ('RF-007','Gestión de inventario','Bibliotecario','alta','propuesto','Por confirmar')]
for r in rf:
    c=table.add_row().cells
    c[0].text=r[0]; c[1].text=r[1]; c[2].text=r[2]; c[3].text=r[3]; c[4].text=r[4]; c[5].text=r[5]

# 5. Requerimientos no funcionales
doc.add_heading('5. Requerimientos no funcionales', level=1)
doc.add_paragraph('RNF-001 Rendimiento: consultas con AsNoTracking/Select (Implementado parcial)')
doc.add_paragraph('RNF-002 Concurrencia: control con índice único y FOR UPDATE/transacciones (Implementado en API)')
doc.add_paragraph('RNF-003 Offline: Room para solicitudes pendientes (Implementado parcial)')
doc.add_paragraph('RNF-004 Seguridad: sin exposición de secretos detectada (Por confirmar)')
doc.add_paragraph('RNF-005 Usabilidad: Jetpack Compose UI (Implementado)')

# 6. Casos de uso
doc.add_heading('6. Casos de uso (identificados)', level=1)
doc.add_paragraph('CU-001 Iniciar sesión (parcial)')
doc.add_paragraph('CU-002 Consultar catálogo (implementado)')
doc.add_paragraph('CU-003 Solicitar préstamo online/offline (parcial)')
doc.add_paragraph('CU-004 Ver mis préstamos (implementado)')
doc.add_paragraph('CU-005 Sincronizar pendientes (parcial)')

# 7. Arquitectura
doc.add_heading('7. Arquitectura', level=1)
doc.add_heading('7.1 Arquitectura general', level=2)
doc.add_paragraph('Cliente Móvil (Android/Kotlin/Compose) -> API REST .NET (Controllers/EF Core) -> PostgreSQL. Web estática servida por API.')
doc.add_heading('7.2 API .NET', level=2)
doc.add_paragraph('Patrón MVC/Controllers, DbContext, DTOs, Models. Program.cs configura DbContext, JSON, CORS, Swagger, archivos estáticos.')
doc.add_heading('7.3 App Móvil', level=2)
doc.add_paragraph('MVVM: UI (Compose), ViewModels, Repository, Data (Retrofit/Room). Navegación con Navigation Compose.')
doc.add_heading('7.4 Tecnologías', level=2)
doc.add_paragraph('API: .NET, EF Core, Npgsql, Swagger. Móvil: Kotlin, Compose, Retrofit, Room, Navigation, Coroutines/Flow. BD: PostgreSQL.')

# 8. Modelo de datos
doc.add_heading('8. Diseño de base de datos', level=1)
doc.add_heading('8.1 Motor', level=2)
doc.add_paragraph('PostgreSQL (comprobable en bibliotecnm.sql y configuración)')
doc.add_heading('8.2 Esquema actual (resumido)', level=2)
doc.add_paragraph('Tablas: usuarios, libros, autores, generos, libros_autores, libros_generos, ejemplares, prestamos. Vista: vista_libros. Índices y restricción única para préstamo activo por ejemplar.')
doc.add_heading('8.3 Esquema propuesto (pendiente)', level=2)
doc.add_paragraph('Por confirmar: campos para renovaciones, notificaciones, auditoría.')

# 9. Interfaces e integraciones
doc.add_heading('9. Interfaces e integraciones', level=1)
doc.add_paragraph('REST API: /api/Auth/login, /api/VistaLibros, /api/Prestamos, /api/Prestamos/solicitar, etc. (verificados)')
doc.add_paragraph('Web estática en wwwroot. CORS configurado para localhost:5500.')
doc.add_paragraph('Integraciones externas: ninguna evidente.')

# 10. Seguridad y permisos
doc.add_heading('10. Seguridad y permisos', level=1)
doc.add_paragraph('Auth: endpoint login existe (AuthController). Password hashing mencionado (PasswordHasher.cs). Roles en BD (lector/bibliotecario/admin).')
doc.add_paragraph('API: validaciones, transacciones, control concurrencia.')
doc.add_paragraph('Riesgos por confirmar: manejo de tokens/sesión móvil.')

# 11. Ejecución y despliegue
doc.add_heading('11. Ejecución y despliegue', level=1)
doc.add_paragraph('API: .NET (ver build.gradle.kts/config). Móvil: Android/Gradle. BD: PostgreSQL con bibliotecnm.sql. Pasos por confirmar.')

# 12. Estrategia de pruebas
doc.add_heading('12. Estrategia de pruebas', level=1)
doc.add_paragraph('Propuestas: unitarias (ViewModels/Repos), integración (API), funcionales (flujos préstamo), concurrencia, offline/sync.')

# 13. Matriz de trazabilidad
doc.add_heading('13. Matriz de trazabilidad', level=1)
doc.add_paragraph('RF-002/RF-004 implementados; RF-003/RF-005 parciales; RF-006/RF-007 propuestos.')

# 14. Pendientes y plan
doc.add_heading('14. Pendientes y plan de implementación', level=1)
doc.add_paragraph('Alta: completar flujo devolución, robustecer auth móvil, pruebas concurrencia.')
doc.add_paragraph('Media: notificaciones, renovación.')
doc.add_paragraph('Baja: mejoras UI.')

# 15. Glosario
doc.add_heading('15. Glosario y referencias', level=1)
doc.add_paragraph('Evidencia: bibliotecnm.sql, API_bibliotecnm/*, Bibliotecnm/app/src/*, plan.md, CONTEXTO.md')

doc.save('E:/API/Documento_Requerimientos_Arquitectura.docx')
print('done')
