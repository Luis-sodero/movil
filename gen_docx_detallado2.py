# -*- coding: utf-8 -*-
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from datetime import datetime

doc = Document()
title = doc.add_heading('Explicacion Detallada del Codigo - App Movil Bibliotecnm', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('Generado: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Ruta base: E:\\API\\Bibliotecnm\\app\\src\\main\\java\\com\\example\\bibliotecnm')
doc.add_paragraph('')

doc.add_heading('1. MainActivity.kt', level=1)
doc.add_paragraph('Clase MainActivity hereda ComponentActivity.')
doc.add_paragraph('onCreate: enableEdgeToEdge(), obtiene app, DataBaseLocal.getDatabase, dao, PrestamoRepository(dao, RetrofitClient.apiService), ConnectivityManager, SyncManager(repo, lifecycleScope), setContent con BibliotecnmTheme y AppNavigation.')
doc.add_paragraph('onStart: registra SyncManager con cm, lanza sincronizarPendientes().')
doc.add_paragraph('onStop: desregistra SyncManager.')

doc.add_heading('2. AppNavigation.kt', level=1)
doc.add_paragraph('Objeto Rutas: LOGIN, PRINCIPAL, PERFIL.')
doc.add_paragraph('AppNavigation(): rememberNavController(), inicio segun SesionUsuario.estaActiva(). NavHost con tres composables controlando backstack.')

doc.add_heading('3. ui/LoginScreen.kt', level=1)
doc.add_paragraph('Formulario correo/password, mostrarPassword, inicia sesion via LoginViewModel. Al LoginUiState.Exito navega. Muestra carga y errores.')

doc.add_heading('4. PantallaPrincipal (MainActivity.kt)', level=1)
doc.add_paragraph('Muestra catalogo con SearchBar. Recarga ON_RESUME y cada 30s. PullToRefresh. Al solicitar abre dialogo y llama VM. Maneja estados solicitud con Toast/Snackbar.')

doc.add_heading('5. PantallaPerfil (PerfilActivity.kt)', level=1)
doc.add_paragraph('Muestra datos SesionUsuario y lista prestamos. TopAppBar con volver/refresh. Menu con cerrar sesion.')

doc.add_heading('6. ViewModels', level=1)
doc.add_paragraph('LoginViewModel: valida, llama AuthRepository, guarda sesion, estados UI.')
doc.add_paragraph('LibrosViewModel: carga libros, solicita prestamo mapeando ResultadoPrestamo. Conserva datos en fallo de red si ya hay lista.')
doc.add_paragraph('PrestamosViewModel: carga prestamos usuario, delega operaciones.')

doc.add_heading('7. data/ApiModels.kt', level=1)
doc.add_paragraph('DTOs con SerializedName. ResultadoPrestamo sealed para tipar resultado de solicitud.')

doc.add_heading('8. data/ApiService.kt y RetrofitClient.kt', level=1)
doc.add_paragraph('ApiService define endpoints. RetrofitClient singleton con OkHttp timeouts y Gson.')

doc.add_heading('9. data/AuthRepository.kt', level=1)
doc.add_paragraph('Login: POST, parsea errores JSON, valida id en respuesta.')

doc.add_heading('10. data/PrestamoRepository.kt', level=1)
doc.add_paragraph('pedirPrestamo: valida sesion/duplicados, intenta API. IOException -> guarda en Room si no existe -> GuardadoSinConexion.')
doc.add_paragraph('sincronizarPrestamo: reintenta con timestamp original, actualiza estado en Room segun respuesta.')
doc.add_paragraph('obtenerLibros/obtenerMisPrestamos: llaman API y retornan Result.')

doc.add_heading('11. Room (Entity/DAO/DB)', level=1)
doc.add_paragraph('PrestamoPendiente almacena solicitudes pendientes. DAO gestiona estados PENDIENTE/RECHAZADO_SIN_EJEMPLARES/SINCRONIZADO/ERROR. DataBaseLocal version 3.')

doc.add_heading('12. SyncManager.kt', level=1)
doc.add_paragraph('Escucha red con NetworkCallback, sincroniza pendientes al conectar.')

doc.add_heading('13. SesionUsuario.kt', level=1)
doc.add_paragraph('Singleton en memoria para usuario logueado.')

doc.add_heading('14. Flujo completo', level=1)
doc.add_paragraph('Login -> guardar sesion -> catalogo. Solicitar online OK o offline guardado. Al reconectar sincroniza automaticamente. Ver prestamos en perfil.')

doc.save('E:/API/Explicacion_Detallada_Codigo_Bibliotecnm.docx')
print('done')
