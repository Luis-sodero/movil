from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt
from datetime import datetime

doc = Document()
title = doc.add_heading('Explicacion Detallada del Codigo - App Movil Bibliotecnm', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('Generado: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Ruta base: E:\\API\\Bibliotecnm\\app\\src\\main\\java\\com\\example\\bibliotecnm')
doc.add_paragraph('')

doc.add_heading('1. MainActivity.kt', level=1)
doc.add_paragraph('Clase MainActivity hereda ComponentActivity.')
doc.add_paragraph('- onCreate: habilita edgeToEdge(), obtiene instancia de app, obtiene DataBaseLocal.getDatabase(app), dao.prestamoPendienteDao(), crea PrestamoRepository(dao, RetrofitClient.apiService), obtiene ConnectivityManager, crea SyncManager(repo, lifecycleScope) con callback vacio, setContent con BibliotecnmTheme y AppNavigation.')
doc.add_paragraph('- onStart: registra SyncManager con ConnectivityManager, lanza sincronizarPendientes() en lifecycleScope.')
doc.add_paragraph('- onStop: desregistra SyncManager.')

doc.add_heading('2. AppNavigation.kt', level=1)
doc.add_paragraph('Objeto Rutas con LOGIN, PRINCIPAL, PERFIL.')
doc.add_paragraph('AppNavigation(): crea rememberNavController(), inicio = PRINCIPAL si SesionUsuario.estaActiva() else LOGIN. NavHost con tres composables.')
doc.add_paragraph('- LOGIN: PantallaLogin con onLoginExitoso navegando a PRINCIPAL con popUpTo(Rutas.LOGIN){inclusive=true}')
doc.add_paragraph('- PRINCIPAL: PantallaPrincipal con onIrAPerfil -> navegar a PERFIL')
doc.add_paragraph('- PERFIL: PantallaPerfil con onVolver popBackStack() y onCerrarSesion que llama SesionUsuario.cerrar() y navega a LOGIN con popUpTo(navController.graph.id){inclusive=true}')

doc.add_heading('3. ui/LoginScreen.kt', level=1)
doc.add_paragraph('PantallaLogin(loginViewModel: LoginViewModel = viewModel()):')
doc.add_paragraph('- Estados: correo/password con rememberSaveable, mostrarPassword, uiState.collectAsState()')
doc.add_paragraph('- iniciarSesion = { loginViewModel.login(correo,password) }')
doc.add_paragraph('- LaunchedEffect(uiState): si Exito llama onLoginExitoso()')
doc.add_paragraph('- UI: gradiente, logo, card con campos Email/Password, boton Entrar con CircularProgressIndicator si cargando, muestra error desde uiState.Error, texto con usuario de prueba')
doc.add_paragraph('Uso de KeyboardOptions/Actions, validacion delegada al ViewModel.')

doc.add_heading('4. MainActivity.kt - PantallaPrincipal', level=1)
doc.add_paragraph('PantallaPrincipal con librosViewModel: LibrosViewModel = viewModel().')
doc.add_paragraph('- Estados locales: textSearch, activeSearch, libroSeleccionado, mostrarDialogo')
doc.add_paragraph('- Estados VM: uiState, solicitudState, refrescando')
doc.add_paragraph('- DisposableEffect(lifecycleOwner): observer ON_RESUME -> librosViewModel.cargarLibros(mostrarCargando=false)')
doc.add_paragraph('- LaunchedEffect(Unit): loop cada 30_000ms recarga')
doc.add_paragraph('- LaunchedEffect(solicitudState): maneja Exito (Toast), GuardadoSinConexion (Snackbar), Error (Toast), luego resetSolicitudState()')
doc.add_paragraph('- UI: Scaffold con SearchBar, boton Refresh, boton Person (ir a perfil). Si no buscando, muestra "Títulos Recomendados".')
doc.add_paragraph('- Cuando uiState.Exito: PullToRefreshBox con LazyColumn de cards. Cada card muestra titulo/autor/generos/anio/disponibilidad. Boton Solicitar habilitado si disponibles>0 y solicitudState no es Cargando.')
doc.add_paragraph('- Dialogo AlertDialog confirma solicitud -> llama librosViewModel.solicitarPrestamo(idLibro, tituloLibro)')

doc.add_heading('5. PerfilActivity.kt', level=1)
doc.add_paragraph('PantallaPerfil: obtiene usuario = SesionUsuario.actual, prestamosViewModel.')
doc.add_paragraph('- Recarga ON_RESUME y cada 30s igual que principal')
doc.add_paragraph('- TopAppBar con volver y refresh')
doc.add_paragraph('- Muestra card con datos usuario si existe')
doc.add_paragraph('- Segun PrestamosUiState muestra cargando/error/lista con LazyColumn de ItemPrestamo')
doc.add_paragraph('- ItemPrestamo: muestra titulo, autor, "Devuelto" o "Devolver el: fechaLimite" con color segun estado')
doc.add_paragraph('- Boton "Configuración de Perfil" abre DropdownMenu con Cambiar información (sin accion) y Cerrar sesión (llama onCerrarSesion)')

doc.add_heading('6. ViewModels', level=1)
doc.add_heading('6.1 LoginViewModel.kt', level=2)
doc.add_paragraph('sealed class LoginUiState { Inactivo, Cargando, Exito(LoginResponse), Error(String) }')
doc.add_paragraph('LoginViewModel: repository = AuthRepository(), _uiState MutableStateFlow.')
doc.add_paragraph('login(correo,password): valida campos vacios -> Error. Si ok set Cargando, llama repository.login. onSuccess guarda SesionUsuario.iniciar(usuario) y set Exito. onFailure set Error con mensaje.')
doc.add_paragraph('limpiarError(): si Error vuelve a Inactivo.')

doc.add_heading('6.2 LibrosViewModel.kt', level=2)
doc.add_paragraph('sealed LibrosUiState { Cargando, Exito(List<LibroVistaDto>), Error(String) }')
doc.add_paragraph('sealed SolicitudUiState { Idle, Cargando, Exito(String), GuardadoSinConexion, Error(String) }')
doc.add_paragraph('AndroidViewModel con repository lazy desde Room+RetrofitClient.')
doc.add_paragraph('cargarLibros(mostrarCargando): si mostrarCargando y no hay Exito pone Cargando; si no, activa refrescando. Llama repository.obtenerLibros(). onSuccess set Exito. onFailure: si IOException y ya hay Exito conserva datos; si IOException sin datos -> Error "Sin conexión, mostrando datos guardados"; otros errores -> Error.')
doc.add_paragraph('solicitarPrestamo(libroId,titulo): set Cargando, llama repo.pedirPrestamo. Mapea ResultadoPrestamo a SolicitudUiState (Confirmado->Exito, GuardadoSinConexion->GuardadoSinConexion, SinEjemplares->Error, UsuarioNoEncontrado->Error, SesionInvalida->Error, ErrorDesconocido->Error). Si Confirmado recarga libros sin cargando.')
doc.add_paragraph('resetSolicitudState() -> Idle.')

doc.add_heading('6.3 PrestamosViewModel.kt', level=2)
doc.add_paragraph('AndroidViewModel, repository lazy. _uiState y _resultadoPrestamo SharedFlow.')
doc.add_paragraph('cargarPrestamos(usuarioId = SesionUsuario.id, mostrarCargando): similar a LibrosViewModel (conserva datos en IOException si hay Exito).')
doc.add_paragraph('pedirPrestamo delega a repo y emite a resultadoPrestamo; si Confirmado recarga.')
doc.add_paragraph('getRechazadosSinAvisar() y marcarAvisado(id) delegan al repo (DAO).')

doc.add_heading('7. Capa data - Modelos y API', level=1)
doc.add_heading('7.1 ApiModels.kt', level=2)
doc.add_paragraph('DTOs con @SerializedName para mapear JSON (camelCase). LoginRequest/Response, LibroVistaDto (id,titulo,anio,autores,generos,ejemplaresTotal,ejemplaresDisponibles), PrestamoDto (id,titulo,autor,fechaLimite,fechaDevolucion), SolicitudPrestamoRequest con timestampSolicitud por defecto, SolicitudPrestamoResponse, MensajeDto.')
doc.add_paragraph('sealed class ResultadoPrestamo con Confirmado, GuardadoSinConexion, SinEjemplares, UsuarioNoEncontrado, SesionInvalida, ErrorDesconocido.')

doc.add_heading('7.2 ApiService.kt', level=2)
doc.add_paragraph('Interface Retrofit: @POST("api/Auth/login") login(LoginRequest): Response<LoginResponse>; @GET("api/VistaLibros") getVistaLibros(): List<LibroVistaDto>; @GET("api/Prestamos") getPrestamos(@Query("usuarioId") usuarioId: Int): List<PrestamoDto>; @POST("api/Prestamos/solicitar") solicitarPrestamo(SolicitudPrestamoRequest): Response<SolicitudPrestamoResponse>')

doc.add_heading('7.3 RetrofitClient.kt', level=2)
doc.add_paragraph('object singleton: BASE_URL "https://gorgeous-refract-provoking.ngrok-free.dev/", OkHttpClient con connect/read/write timeout 10/15/15s, Retrofit con GsonConverterFactory.')

doc.add_heading('7.4 AuthRepository.kt', level=2)
doc.add_paragraph('login(correo,password): llama apiService.login con trim. Si no exitoso parsea errorBody a MensajeDto (Gson), mapea codigo 401 o genera mensaje con codigo. Si exitoso valida que usuario?.id != null, sino error. Catch general devuelve mensaje generico.')

doc.add_heading('8. PrestamoRepository.kt (detalle)', level=1)
doc.add_paragraph('Clase con dao opcional y apiService por defecto. Mutex syncMutex.')
doc.add_paragraph('guardarPrestamoOffline: si sin dao lanza error; verifica existentes con contarPendientesPorLibroYUsuario -> si hay devuelve null; inserta PrestamoPendiente estado PENDIENTE, avisado false, devuelve copia con id.')
doc.add_paragraph('pendientes() getPendientesList(), observarPendientes() getPendientes(), getRechazadosSinAvisar(), marcarAvisado, actualizarEstado delegan a dao.')
doc.add_paragraph('pedirPrestamo(libroId,titulo): obtiene usuarioId = SesionUsuario.id; si <=0 -> SesionInvalida. Revisa duplicados locales.')
doc.add_paragraph('Try API: solicita con SolicitudPrestamoRequest(libroId, usuarioId, timestamp). Si exitoso y body != null -> Confirmado(body). Si no: 409 -> SinEjemplares, 404 -> UsuarioNoEncontrado, else ErrorDesconocido(codigo).')
doc.add_paragraph('Catch IOException: intenta guardarPrestamoOffline; si null (duplicado) -> ErrorDesconocido duplicado; si guardado ok -> GuardadoSinConexion. Catch general -> ErrorDesconocido.')
doc.add_paragraph('sincronizarPrestamo(p): con syncMutex intenta API con timestamp original. Si exitoso -> dao.actualizarEstado(p.id, "SINCRONIZADO") y success. Si 409 -> "RECHAZADO_SIN_EJEMPLARES", 404 -> "ERROR", else error.')
doc.add_paragraph('obtenerMisPrestamos: llama apiService.getPrestamos(usuarioId) -> Result.success/failure.')
doc.add_paragraph('obtenerLibros: llama apiService.getVistaLibros() -> Result.success/failure.')

doc.add_heading('9. Room - Entidad, DAO, DB', level=1)
doc.add_heading('9.1 Entity.kt', level=2)
doc.add_paragraph('@Entity(tableName="prestamosPendientes") PrestamoPendiente con @PrimaryKey(autoGenerate=true) id Long, libroId Int, libroTitulo String, usuarioId Int, timestampClic Long, estado String="PENDIENTE", avisado Boolean=false.')

doc.add_heading('9.2 DAO.kt', level=2)
doc.add_paragraph('PrestamoPendienteDao: @Insert suspend insert: Long.')
doc.add_paragraph('@Query pendientes Flow y List: estado PENDIENTE ordenados por timestampClic ASC.')
doc.add_paragraph('contarPendientesPorLibroYUsuario cuenta PENDIENTE por libroId/usuarioId.')
doc.add_paragraph('actualizarEstado actualiza estado por id.')
doc.add_paragraph('getRechazadosSinAvisar: RECHAZADO_SIN_EJEMPLARES AND avisado=0.')
doc.add_paragraph('marcarAvisado set avisado=1.')
doc.add_paragraph('eliminar por id.')

doc.add_heading('9.3 DataBaseLocal.kt', level=2)
doc.add_paragraph('@Database(entities=[PrestamoPendiente::class], version=3, exportSchema=false). abstract fun prestamoPendienteDao().')
doc.add_paragraph('Companion object con INSTANCE volatile, getDatabase crea Room.databaseBuilder con fallbackToDestructiveMigration(), guarda singleton.')

doc.add_heading('10. SyncManager.kt', level=1)
doc.add_paragraph('Recibe repository, scope, onSynced opcional.')
doc.add_paragraph('callback NetworkCallback.onAvailable lanza scope.launch { sincronizarPendientes() }')
doc.add_paragraph('sincronizarPendientes(): obtiene pendientes = repository.pendientes(), itera y llama sincronizarPrestamo(p); si algun res.isSuccess marca huboSincronizados y llama onSynced()')
doc.add_paragraph('register(cm): registra default network callback si no registrado. unregister similar.')

doc.add_heading('11. SesionUsuario.kt', level=1)
doc.add_paragraph('object con usuario volatile LoginResponse?. actual get, id get = usuario?.id ?: 0. iniciar(usuario) set, cerrar() null, estaActiva() id>0.')

doc.add_heading('12. Resumen del flujo completo', level=1)
doc.add_paragraph('Login: UI -> VM -> AuthRepo -> API -> guarda sesion -> navega a principal.')
doc.add_paragraph('Ver catalogo: VM carga al inicio/ON_RESUME/cada 30s/refresh -> Repo -> API GET VistaLibros -> UI lista.')
doc.add_paragraph('Solicitar: confirma -> VM -> Repo valida/llama API. Online OK -> Confirmado actualiza estado y recarga. Offline -> guarda Room -> GuardadoSinConexion.')
doc.add_paragraph('Sincronizar: red disponible -> SyncManager sincroniza pendientes -> actualiza estados en Room.')
doc.add_paragraph('Ver prestamos: Perfil carga mis prestamos via Repo GET Prestamos por usuarioId.')

doc.save('E:/API/Explicacion_Detallada_Codigo_Bibliotecnm.docx')
print('done')
