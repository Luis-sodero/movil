PLAN: ANDROID \- SOLICITAR UN PRESTAMO DESDE EL EMULADOR (PRUEBA MINIMA)

OBJETIVO  
Que la app Bibliotecnm liste los libros reales desde la API y permita solicitar  
un prestamo de un libro desde el emulador. Nada mas. Es una prueba funcional,  
no la version final.

ESTADO ACTUAL (ya hecho, no tocar)  
\- La API ya acepta peticiones y funciona en http\://localhost:5023  
\- Endpoints disponibles:  
  GET  /api/VistaLibros  
  POST /api/Prestamos/solicitar   body: { "libroId": int, "usuarioId": int }  
  Respuestas del POST: 200 { id, ejemplarId, fechaLimite } | 404 usuario no  
  existe | 409 { mensaje } si no hay ejemplares libres.  
\- Navegacion con NavHost ya implementada. No modificarla.

DECISIONES YA TOMADAS (no cambiarlas)  
\- Usuario fijo: usuarioId \= 1\. Sin login ni JWT.  
\- Retrofit 2.11 \+ converter-gson.  
\- Base URL: http\://10.0.2.2:5023/ (10.0.2.2 es la PC vista desde el emulador).

ANTES DE ESCRIBIR CODIGO  
\- Abrir http\://localhost:5023/swagger o llamar a GET /api/VistaLibros y copiar  
  los nombres REALES de los campos del JSON. Los DTOs de Kotlin deben coincidir  
  exactamente (camelCase). No asumir nombres.

ALCANCE: LO QUE SI SE HACE  
1\. Permiso de internet y configuracion de trafico HTTP en el emulador.  
2\. Capa de red minima con Retrofit (2 endpoints).  
3\. Un ViewModel que carga los libros y solicita el prestamo.  
4\. Conectar la pantalla principal para listar libros reales y activar el boton  
   "Solicitar Prestamo".

ALCANCE: LO QUE NO SE HACE (no implementar)  
\- Pantalla de perfil: se queda con sus datos de ejemplo, sin tocarla.  
\- Login, registro, JWT, seguridad.  
\- Room / cache local. No tocar KSP ni sus dependencias.  
\- Imagenes de portada (se mantiene el recuadro actual).  
\- Busqueda funcional (la barra queda como esta).  
\- Devolucion de libros, renovaciones, notificaciones.  
\- Inyeccion de dependencias (Hilt/Koin). Instanciar manualmente.  
\- Cambios de diseno visual ni refactors no pedidos.  
\- Pruebas automatizadas.

PASOS

1\. AndroidManifest.xml  
\- Agregar fuera de \<application\>:  
  \<uses-permission android:name="android.permission.INTERNET" /\>  
\- Agregar dentro de \<application\>:  
  android:networkSecurityConfig="@xml/network\_security\_config"

2\. Crear res/xml/network\_security\_config.xml  
\- Permitir trafico HTTP sin cifrar SOLO para el dominio 10.0.2.2  
  (cleartextTrafficPermitted="true"). Sin esto Android bloquea las peticiones.  
  Este archivo se elimina cuando la API use HTTPS en la nube.

3\. Dependencias (libs.versions.toml \+ app/build.gradle.kts)  
\- retrofit 2.11.x  
\- converter-gson 2.11.x  
\- androidx.lifecycle:lifecycle-viewmodel-compose (reusar la version del  
  catalogo de lifecycle que ya existe)  
\- Usar el version catalog, sin versiones sueltas en el modulo.

4\. Nuevo paquete data/  
\- ApiModels.kt:  
  \* LibroVistaDto (campos segun el JSON real de /api/VistaLibros; incluir  
    ejemplaresDisponibles).  
  \* SolicitudPrestamo(libroId, usuarioId).  
  \* RespuestaPrestamo(id, ejemplarId, fechaLimite).  
  Gson no respeta los tipos no nulos de Kotlin: declarar como nullable (?)  
  cualquier campo que pueda venir vacio (anio, sinopsis, etc.).  
\- ApiService.kt: interfaz Retrofit con funciones suspend:  
  getLibros(): List\<LibroVistaDto\>  
  solicitarPrestamo(@Body SolicitudPrestamo): RespuestaPrestamo  
\- RetrofitClient.kt: object con la base URL y GsonConverterFactory.  
\- PrestamoRepository.kt:  
  obtenerLibros() y solicitarPrestamo(libroId) con usuarioId \= 1\.  
  Si la API responde 409 (Retrofit lanza HttpException), devolver un resultado  
  "no disponible" en lugar de dejar que la excepcion llegue a la UI. Cualquier  
  otro fallo de red se devuelve como error con mensaje.

5\. LibrosViewModel (viewModelScope \+ StateFlow)  
\- Estado de la lista: Cargando / Exito(libros) / Error(mensaje).  
\- Funcion solicitar(libroId) con un estado para el resultado  
  (ninguno / exito / no disponible / error) que la UI muestre una vez.  
\- Tras una solicitud exitosa, recargar la lista para que ejemplaresDisponibles  
  se actualice.

6\. Pantalla principal (cambios minimos)  
\- Reemplazar listaRecomendados por los libros de GET /api/VistaLibros.  
\- Mapear el DTO al modelo que usa la UI solo lo necesario. "generos" y  
  "autores" llegan como texto separado por comas, igual que antes.  
\- Mostrar un indicador de carga y un mensaje si falla la red.  
\- Boton "Solicitar Prestamo" del dialogo: llama a solicitar(libroId).  
  Deshabilitarlo si ejemplaresDisponibles \== 0 (reemplaza la condicion  
  actual basada en "disponible").  
\- Mostrar el resultado de la solicitud con un Snackbar o Toast  
  ("Prestamo registrado" / "No hay ejemplares disponibles" / mensaje de error)  
  y cerrar el dialogo.  
\- No bloquear la UI mientras se envia: evitar que se pueda pulsar el boton dos  
  veces seguidas (deshabilitarlo mientras la peticion esta en curso).

VERIFICACION (en el emulador, con la API corriendo)  
1\. Abrir http\://10.0.2.2:5023/swagger en el navegador del emulador. Si no  
   carga, revisar firewall de Windows o el puerto antes de tocar el codigo.  
2\. La app lista los 5 libros reales. 1984 y Dracula aparecen no disponibles.  
3\. Solicitar "El Alquimista": aparece "Prestamo registrado" y tras la recarga  
   el libro queda no disponible.  
4\. Confirmar en la BD: SELECT \* FROM prestamos;  (debe haber una fila nueva  
   con usuario\_id \= 1).  
5\. Intentar solicitar un libro sin ejemplares libres: la app muestra  
   "No hay ejemplares disponibles" y no se cae.  
6\. Apagar la API y reabrir la app: debe mostrar el mensaje de error de red,  
   sin cerrarse.

CRITERIO DE TERMINADO  
Los puntos 2 a 6 funcionan en el emulador. Al terminar, resumir que archivos se  
crearon o modificaron y no agregar nada fuera de este alcance.  
