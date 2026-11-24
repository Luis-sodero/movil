CONTEXTO  
App Android "Bibliotecnm" (Kotlin \+ Compose). Ya se hizo el diagnóstico (Fase 1). Ahora implementa la Fase 2 con cambios mínimos, código simple de nivel estudiante y comentarios breves en español. No agregues librerías nuevas salvo que sea estrictamente necesario (si lo es, dime cuál y por qué antes de agregarla). NO modifiques el backend.

1\. ACTUALIZACIÓN DINÁMICA DE LIBROS (MainActivity.kt \+ LibrosViewModel.kt)  
a) En LibrosViewModel cambia cargarLibros() para recibir un parámetro (por ejemplo mostrarCargando: Boolean \= true).  
   \- Si mostrarCargando es true, pone LibrosUiState.Cargando como ahora.  
   \- En las recargas silenciosas NO pongas Cargando ni borres la lista actual.  
   \- Si una recarga silenciosa falla (IOException), conserva el estado anterior y no cambies a pantalla de error.  
   \- Agrega un StateFlow\<Boolean\> "refrescando" para el pull-to-refresh.  
b) En PantallaPrincipal agrega una recarga automática mientras la pantalla está visible, usando el ciclo de vida:  
   val lifecycleOwner \= LocalLifecycleOwner.current  
   LaunchedEffect(lifecycleOwner) {  
       lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {  
           while (true) {  
               librosViewModel.cargarLibros(mostrarCargando \= false)  
               delay(30\_000)  
           }  
       }  
   }  
   Esto recarga al volver a la app y cada 30 s, y se detiene sola en segundo plano. Evita la doble carga al arrancar: la primera carga debe seguir mostrando "Cargando" y las siguientes ser silenciosas (por ejemplo, quita el init { cargarLibros() } o pasa mostrarCargando solo si el estado actual no es Exito). Si repeatOnLifecycle no está disponible por dependencias, usa DisposableEffect con LifecycleEventObserver (ON\_RESUME) \+ LaunchedEffect con delay(30\_000).  
c) Pull-to-refresh en la lista de libros con PullToRefreshBox de Material3 (con @OptIn(ExperimentalMaterial3Api::class)). Si la versión de Material3 del proyecto no lo incluye, agrega un IconButton "Actualizar" en la barra superior que llame a cargarLibros(). Dime cuál de las dos opciones usaste.  
d) Después de solicitar un préstamo con éxito, ya se llama a cargarLibros(): déjalo, en modo silencioso.

2\. ACTUALIZACIÓN DINÁMICA DEL PERFIL (PerfilActivity.kt \+ PrestamosViewModel.kt)  
Aplica exactamente lo mismo: cargarPrestamos(mostrarCargando), recarga con repeatOnLifecycle cada 30 s, pull-to-refresh y conservar los datos si falla por falta de conexión.

3\. SOLICITUD SIN CONEXIÓN: UNIFICAR EL FLUJO  
a) El botón "Solicitar" (MainActivity \-\> LibrosViewModel.solicitarPrestamo) debe pasar por PrestamoRepository.pedirPrestamo(libroId, titulo), que ya toma System.currentTimeMillis() ANTES de la llamada, manda timestampSolicitud y captura IOException guardando en Room.  
   \- Cambia LibrosViewModel.solicitarPrestamo para recibir también el título del libro (disponible en libroSeleccionado) y llamar a pedirPrestamo.  
   \- Mapea ResultadoPrestamo a SolicitudUiState y agrega un estado nuevo (por ejemplo SolicitudUiState.GuardadoSinConexion). La UI debe mostrar: "Sin conexión. Tu solicitud se guardó y se enviará cuando vuelvas a tener internet." Nunca muestres el texto crudo de la excepción (por ejemplo "Unable to resolve host").  
   \- IMPORTANTE: verifica que pedirPrestamo conserve el manejo de respuestas del servidor que tenía solicitarPrestamo (409 "No hay ejemplares libres", 404 "Usuario no encontrado", etc.). Un error HTTP del servidor NO debe guardarse como pendiente; solo los IOException (sin internet, timeout).  
   \- Revisa que la sesión inválida (SesionUsuario.id \<= 0\) siga devolviendo su mensaje.  
b) Evita duplicados: antes de guardar en guardarPrestamoOffline, revisa si ya existe una pendiente (estado pendiente) con el mismo libroId y usuarioId. Si existe, no insertes otra y avisa "Ya tienes una solicitud pendiente de este libro". Agrega la consulta necesaria al DAO.  
c) Deja solicitarPrestamo antiguo del repositorio solo si algo más lo usa; si ya no lo usa nadie, elimínalo y dime que lo hiciste.

4\. SINCRONIZAR AL RECONECTAR (SyncManager.kt)  
a) Registra SyncManager, que hoy nadie instancia:  
   \- Revisa su código. Si no tiene métodos para registrar y para detener el callback, agrégalos (registerDefaultNetworkCallback / unregisterNetworkCallback).  
   \- Instáncialo y regístralo en MainActivity (por ejemplo en onStart/onCreate y desregístralo en onStop/onDestroy), o en una clase Application si el proyecto ya tiene una. Que no se registre dos veces.  
b) Además de al reconectar, sincroniza las pendientes al abrir la app y en cada ON\_RESUME de la pantalla principal (una sola llamada a un método tipo sincronizarPendientes() de SyncManager).  
c) Procesa las pendientes ordenadas por timestampClic ascendente, una por una (revisa que getPendientesList lo haga).  
d) Al terminar de sincronizar al menos una: recarga libros y préstamos (silencioso) para reflejar el cambio.  
e) Los avisos de solicitudes rechazadas porque el ejemplar ya no estaba disponible (getRechazadosSinAvisar / marcarAvisado): revisa dónde se muestran hoy. Asegúrate de que se muestren al usuario (Toast, Snackbar o diálogo) después de una sincronización y también al abrir la app, y que se marquen como avisadas para no repetirse.  
f) Si una pendiente vuelve a fallar por IOException, déjala en Room para el siguiente intento.

5\. RETROFIT  
En RetrofitClient.kt agrega un OkHttpClient con timeouts razonables (connectTimeout 10 s, readTimeout 15 s, writeTimeout 15 s) y úsalo en Retrofit.Builder().client(...), para que sin internet falle rápido y no deje la pantalla cargando. No cambies la BASE\_URL.

6\. TIMESTAMP Y BACKEND (solo reportar)  
a) Dime exactamente qué tipo y formato lleva timestampSolicitud en SolicitudPrestamoRequest (Long en milisegundos, String ISO 8601, etc.).  
b) Si tienes acceso al proyecto de la API, revisa el controlador/DTO que recibe api/Prestamos/solicitar: ¿acepta ese campo? ¿con qué tipo? ¿lo guarda en FechaSolicitud? Si es DateTime hacia una columna timestamptz, confirma que se convierta a UTC (Kind=Utc) antes de guardar. No modifiques el backend, solo repórtalo.

ENTREGA  
\- Muestra el diff de cada archivo modificado.  
\- Compila (gradlew assembleDebug) y confirma que no hay errores.  
\- Resume los archivos tocados y cualquier punto pendiente.  
\- Indica cómo probar:  
  (a) Marcar una devolución en la web admin y ver el cambio en la app en máximo 30 s sin cerrarla.  
  (b) Agregar un libro en la web admin y verlo aparecer en la app sin reiniciarla (esperando 30 s o deslizando para actualizar).  
  (c) Modo avión: solicitar un préstamo y ver el mensaje de "guardada" sin errores de host. Intentar solicitar el mismo libro otra vez y ver el aviso de duplicado. Desactivar el modo avión y ver que se envía sola y aparece en el perfil.  
  (d) Caso de rechazo: con modo avión, solicitar el último ejemplar disponible; antes de reconectar, pedirlo desde otro usuario; al reconectar debe avisar que ya no estaba disponible.

