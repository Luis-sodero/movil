# Prompt para generar el documento de requerimientos y arquitectura

Actúa como analista de sistemas, arquitecto de software y especialista en documentación técnica. Analiza la carpeta completa del proyecto que tienes abierta y genera un documento profesional de requerimientos y arquitectura que sirva como base para terminar el sistema.

## Objetivo

El proyecto está incompleto. Debes documentar lo que ya existe y proponer de manera clara lo que falta para completar el sistema. No supongas que las funcionalidades propuestas ya están implementadas.

## Instrucciones generales

1. Inspecciona la estructura de carpetas, código fuente, configuración, dependencias, modelos, controladores, servicios, rutas, vistas, scripts SQL, migraciones, pruebas y documentación existente.
2. Distingue claramente entre:
   - **Implementado:** comprobable en el proyecto.
   - **Parcial:** existe, pero está incompleto o no funciona completamente.
   - **Propuesto:** necesario o recomendable para terminar el sistema.
3. No inventes funcionalidades, reglas de negocio, usuarios, integraciones ni tecnologías. Si un dato no puede comprobarse, indícalo como pendiente de confirmar.
4. No modifiques el código ni la configuración del proyecto. Tu tarea es analizar y generar documentación.
5. No incluyas contraseñas, tokens, claves privadas, cadenas de conexión con credenciales ni otros secretos.
6. Si ya existe un documento con el mismo nombre, crea una nueva versión sin sobrescribir el original.
7. Genera el archivo DOCX físicamente en la carpeta raíz del proyecto. No te limites a mostrar el contenido en el chat.

## Archivo de salida

Crea un documento de Microsoft Word con el nombre:

`Documento_Requerimientos_Arquitectura.docx`

Usa español claro, técnico y profesional. Evita repetir información y señala las decisiones que requieren validación.

## Estructura obligatoria del documento

### 1. Portada
Incluye nombre del proyecto (si se puede identificar), título del documento, versión, fecha de generación y estado del documento. Si no conoces un dato, indícalo como “Por confirmar”.

### 2. Introducción y visión del sistema
- Propósito del documento.
- Problema o necesidad que atiende el sistema.
- Objetivo general.
- Objetivos específicos, solo cuando puedan deducirse de la evidencia; marca las inferencias.
- Descripción general de la solución.

### 3. Alcance
- Funcionalidades que ya existen.
- Funcionalidades parciales.
- Funcionalidades propuestas para completar el sistema.
- Exclusiones y límites conocidos.
- Supuestos y restricciones por validar.

### 4. Usuarios y actores
Identifica los tipos de usuarios y sistemas externos que estén demostrados por el código o la documentación. Para actores propuestos, indica que requieren validación. Resume sus responsabilidades y permisos conocidos o propuestos.

### 5. Requerimientos funcionales
Define requerimientos con identificadores únicos (`RF-001`, `RF-002`, etc.). Para cada uno incluye:
- Nombre y descripción verificable.
- Actor relacionado, si aplica.
- Prioridad (alta, media o baja).
- Estado: implementado, parcial, propuesto o por confirmar.
- Criterios de aceptación comprobables.
No presentes como existente una función que solo sea una propuesta.

### 6. Requerimientos no funcionales
Usa identificadores `RNF-001`, `RNF-002`, etc. Considera, cuando corresponda, seguridad, rendimiento, disponibilidad, usabilidad, accesibilidad, mantenibilidad, compatibilidad, escalabilidad, respaldo y recuperación. No inventes métricas; si no hay valores definidos, registra una propuesta medible y márcala para aprobación.

### 7. Reglas de negocio
Define reglas con identificadores `RN-001`, `RN-002`, etc. Incluye únicamente las que tengan evidencia en el proyecto o documentación. Las reglas sugeridas deben etiquetarse como propuestas y quedar pendientes de validación.

### 8. Casos de uso
- Describe los casos de uso principales con identificadores `CU-001`, `CU-002`, etc.
- Incluye actor, objetivo, precondiciones, flujo principal, alternativas o errores y postcondiciones.
- Añade un diagrama UML de casos de uso si la información disponible lo permite.
- Distingue los casos actuales de los propuestos.

### 9. Arquitectura de software
- Describe la arquitectura observada y las tecnologías detectadas.
- Explica las responsabilidades de las capas o componentes.
- Propón una arquitectura objetivo para completar el sistema, justificando los cambios.
- Incluye un diagrama general de arquitectura.
- Separa claramente el estado actual de la propuesta.

### 10. Diagrama UML de paquetes
Genera un diagrama UML de paquetes que muestre la organización lógica propuesta, sus responsabilidades y dependencias. Basa los paquetes existentes en la estructura real del proyecto; etiqueta como propuesta cualquier paquete nuevo.

### 11. Diagrama UML de componentes
Genera un diagrama UML de componentes con los componentes principales, sus interfaces y dependencias. No representes interfaces o integraciones como existentes si no hay evidencia.

### 12. Diseño de base de datos
- Identifica el motor de base de datos, tablas, entidades, modelos, relaciones, claves y restricciones que puedan verificarse.
- Presenta el modelo actual, si existe.
- Propón las entidades, atributos, tipos de datos, claves primarias, claves foráneas, restricciones y cardinalidades que falten para cubrir los requerimientos propuestos.
- Distingue claramente el esquema actual del esquema propuesto.
- Incluye un diagrama entidad-relación (ER).
- Explica las decisiones relevantes y los aspectos pendientes de validar.
- No afirmes que una base de datos o tabla existe solo porque aparece en una propuesta.

### 13. Diagrama UML de despliegue
Representa los nodos de ejecución, dispositivos, servidores, contenedores, servicios y conexiones que se puedan verificar. Si el despliegue aún no existe, presenta una propuesta identificada como tal. No inventes proveedores ni infraestructura confirmada.

### 14. Interfaces e integraciones
Documenta las interfaces de usuario, rutas, API, servicios externos, formatos de intercambio y dependencias detectadas. Para integraciones futuras, especifica que son propuestas y qué falta para implementarlas.

### 15. Seguridad y permisos
Describe los mecanismos de autenticación, autorización, validación de datos, gestión de sesiones, protección de información y manejo de secretos que estén presentes. Señala riesgos observados y controles recomendados. No expongas secretos encontrados.

### 16. Ejecución y despliegue
Incluye requisitos previos, dependencias, configuración necesaria y pasos de ejecución solo cuando se puedan deducir con suficiente confianza. Separa los pasos verificados de los pendientes de confirmar. No inventes comandos ni credenciales.

### 17. Estrategia de pruebas
Propón pruebas unitarias, de integración, funcionales, de seguridad y de aceptación según la tecnología y el alcance. Indica qué pruebas existen, cuáles faltan y criterios de aceptación relevantes.

### 18. Matriz de trazabilidad
Relaciona requerimientos funcionales, casos de uso, componentes o módulos, entidades de datos y pruebas. Si una relación es propuesta, indícalo.

### 19. Pendientes y plan de implementación
Organiza el trabajo pendiente por prioridad y dependencias. Para cada actividad incluye objetivo, entregable esperado y criterio de finalización. Evita fechas o estimaciones sin evidencia.

### 20. Glosario y referencias
Define términos propios del sistema y enumera los archivos del proyecto usados como evidencia. No inventes fuentes externas.

## Diagramas obligatorios

Incluye como mínimo estos diagramas, legibles y con título:

1. Arquitectura general del sistema.
2. UML de paquetes.
3. UML de componentes.
4. UML de despliegue.
5. Modelo entidad-relación de la base de datos.
6. UML de casos de uso, cuando se puedan identificar actores y funcionalidades suficientes.

Puedes utilizar PlantUML, Mermaid u otra herramienta disponible. Si generas los diagramas con código, expórtalos como imágenes e insértalos en el DOCX. Si una herramienta de exportación no está disponible, incluye el código fuente del diagrama dentro de un apéndice y explica que falta generar la imagen. No presentes un diagrama ilegible ni un archivo de imagen vacío.

Guarda también, si es posible, los archivos fuente de los diagramas en una carpeta `documentacion/diagramas/`. No cambies el código de la aplicación.

## Formato del DOCX

- Portada profesional.
- Tabla de contenido actualizable.
- Títulos y subtítulos con estilos de Word.
- Secciones y tablas numeradas de manera consistente.
- Diagramas insertados como imágenes, con títulos y una breve explicación.
- Encabezado o pie de página con nombre del proyecto y número de página, cuando sea posible.
- Tablas legibles, sin texto cortado ni columnas excesivamente anchas.
- Redacción consistente y sin repeticiones innecesarias.

## Revisión final obligatoria

Antes de terminar:

1. Comprueba que el DOCX exista y pueda abrirse.
2. Verifica que todas las secciones obligatorias estén incluidas.
3. Comprueba que los diagramas sean legibles y coherentes con los requerimientos y el modelo de datos.
4. Revisa que no haya secretos ni credenciales expuestas.
5. Confirma que las propuestas estén diferenciadas de las funcionalidades implementadas.
6. Comprueba que el archivo se haya guardado en la raíz del proyecto.
7. Informa brevemente qué archivo generaste, qué diagramas incluiste y qué aspectos quedaron pendientes de confirmar.

El resultado principal debe ser el archivo `Documento_Requerimientos_Arquitectura.docx`. No te limites a devolver un resumen ni a pegar el documento en el chat.
