Vamos a implementar 3 funciones nuevas en Bibliotecnm (app Android en Kotlin \+ Jetpack Compose, y la API en ASP.NET Core con PostgreSQL). Las implementaciones deben ser SIMPLES, al nivel de un estudiante de 7mo semestre en su primera materia de programación móvil, y deben seguir la lógica, estructura y convenciones que ya tiene el proyecto (mismos patrones de navegación NavHost, Retrofit/llamadas a la API, Room, ViewModel/estado, nombres de carpetas y clases, etc.). No agregues librerías ni arquitecturas nuevas salvo la indicada abajo. No refactorices código que no esté relacionado.

ANTES DE ESCRIBIR CÓDIGO:  
\- Revisa cómo funciona hoy el inicio de sesión (pantalla, ViewModel, endpoint de la API, tabla de usuarios, "recordar usuario" con Room), la pantalla principal con la barra de búsqueda, el modelo/DTO de Libro, y cómo se guardan y se sirven las imágenes de los libros (campo en la BD, controlador, carpeta wwwroot, etc.).  
\- Dime en pocas líneas qué encontraste y qué archivos vas a tocar. Después implementa.

FUNCIÓN 1: CREAR CUENTA  
Android:  
\- En la pantalla de inicio (la que aparece al abrir la app sin sesión), junto al botón "Iniciar sesión" agrega un botón/texto "Crear cuenta".  
\- Crear una pantalla de registro (Composable) con 3 campos: nombre, correo y contraseña, y un botón "Registrarme". Validaciones básicas: campos no vacíos, formato de correo válido, contraseña de mínimo 6 caracteres. Mostrar mensaje de error si el correo ya existe o si falla la red.  
\- Registrar la ruta en el NavHost existente. Si el registro es exitoso, regresar al login o iniciar sesión directamente, usando el mismo flujo que ya existe (incluido "recordar usuario" si aplica).  
API:  
\- Agregar un endpoint POST de registro (en el controlador de usuarios/autenticación que ya exista) que reciba nombre, correo y contraseña, valide que el correo no esté repetido (responder 409 o el código que ya use el proyecto para conflictos) y guarde el usuario como rol lector/usuario normal, NUNCA como administrador.  
\- Guardar la contraseña con el mismo método que ya use el login actual. Si hoy se guarda en texto plano, avísame antes de cambiarlo; no migres nada por tu cuenta.  
\- No implementes JWT ni refresh tokens (está pospuesto).

FUNCIÓN 2: BARRA DE BÚSQUEDA  
\- Hacer que la barra de búsqueda de la pantalla principal filtre los libros mientras se escribe, haciendo match (sin distinguir mayúsculas/minúsculas y sin importar acentos si es fácil) por título, autor o género: si el texto aparece en cualquiera de esos tres campos, el libro se muestra.  
\- Implementación simple: filtrar en la app sobre la lista de libros que ya se tiene cargada (la misma que viene de la API/Room), con un estado de texto de búsqueda y un \`filter\` sobre la lista. No crear endpoint nuevo en la API.  
\- Si el campo está vacío, mostrar todos los libros como hoy. Si no hay resultados, mostrar un mensaje tipo "No se encontraron libros".

FUNCIÓN 3: IMÁGENES DESDE LA API  
\- Hacer que las portadas de los libros se carguen desde la API y se muestren en la app (tarjetas de la pantalla principal y cualquier pantalla de detalle donde aparezca el libro).  
\- En la API: verifica cómo se guarda la imagen hoy. Lo más simple es que el libro tenga un campo con la ruta/nombre del archivo de imagen, que la API sirva los archivos como estáticos (UseStaticFiles sobre una carpeta como wwwroot/images) y que el JSON del libro regrese la URL completa o la ruta relativa. Usa lo que ya exista y completa lo que falte.  
\- En la web de administración, si el formulario "Nuevo libro" aún no permite subir imagen, agrega un campo para subir el archivo y guardarlo en esa carpeta. Si ya existe, no lo toques.  
\- En Android: usa Coil (\`AsyncImage\`) para mostrar la imagen a partir de la URL, con un placeholder/imagen por defecto cuando el libro no tenga portada o falle la carga. Si el proyecto ya usa otra librería de imágenes, usa esa en lugar de Coil.  
\- Asegúrate de que la URL base sea la misma que ya usa la app para la API (cuidado con emulador 10.0.2.2 vs. dispositivo real) y de que el permiso de INTERNET y la configuración de tráfico HTTP (si la API usa http y no https) estén correctos.

AL TERMINAR:  
\- Lista los archivos creados/modificados y qué hace cada cambio, en lenguaje simple.  
\- Indica si hace falta una migración o un cambio en la base de datos (por ejemplo, una columna nueva) y dame el script SQL exacto para PostgreSQL en lugar de ejecutarlo tú.  
\- Dime cómo probar cada función (registro, búsqueda por título/autor/género e imágenes).  
