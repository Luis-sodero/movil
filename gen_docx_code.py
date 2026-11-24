from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from datetime import datetime

doc = Document()
title = doc.add_heading('Explicacion del Codigo a Grandes Razgos - Bibliotecnm', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('Generado: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Ruta: E:\\API\\Bibliotecnm\\app\\src\\main\\java\\com\\example\\bibliotecnm')
doc.add_paragraph('')

doc.add_heading('1. Punto de entrada', level=1)
doc.add_paragraph('MainActivity: crea DB, Repo, SyncManager. Habilita edge-to-edge. En onStart registra red y sincroniza pendientes. Pasa a AppNavigation con tema.')
doc.add_paragraph('AppNavigation: decide startDestination por SesionUsuario.usaactiva(). Maneja login->principal, principal->perfil, logout.')

doc.add_heading('2. UI (Compose)', level=1)
doc.add_paragraph('LoginScreen: formulario con validacion basica, estado de carga, muestra errores del ViewModel.')
doc.add_paragraph('PantallaPrincipal (MainActivity): lista libros con Card, SearchBar, pull-to-refresh, recarga al resume/cada 30s. Solicitud via dialogo.')
doc.add_paragraph('PantallaPerfil (PerfilActivity): muestra usuario + prestamos. Menu para cerrar sesion.')

doc.add_heading('3. ViewModels (MVVM)', level=1)
doc.add_paragraph('LoginViewModel: llama AuthRepository.login, guarda sesion en SesionUsuario, expone LoginUiState.')
doc.add_paragraph('LibrosViewModel: carga catalogo (obtenerLibros) y gestiona solicitud (pedirPrestamo) con estados Exito/GuardadoSinConexion/Error.')
doc.add_paragraph('PrestamosViewModel: carga mis prestamos, delega solicitud al repo.')

doc.add_heading('4. Datos y red', level=1)
doc.add_paragraph('ApiService (Retrofit): define endpoints REST.')
doc.add_paragraph('RetrofitClient: singleton con OkHttp timeouts, base URL ngrok.')
doc.add_paragraph('AuthRepository: maneja respuesta login, parsea errores.')
doc.add_paragraph('PrestamoRepository: decide online/offline. Si IOException guarda en Room. Sincroniza pendientes.')
doc.add_paragraph('ApiModels: DTOs + sealed ResultadoPrestamo para tipar resultados.')

doc.add_heading('5. Offline con Room', level=1)
doc.add_paragraph('PrestamoPendiente (Entity): guarda libroId/titulo/usuarioId/timestamp/estado/avisado.')
doc.add_paragraph('PrestamoPendienteDao: CRUD y consultas por estado.')
doc.add_paragraph('DataBaseLocal: Room DB singleton.')
doc.add_paragraph('SyncManager: escucha conectividad y sincroniza pendientes llamando repository.sincronizarPrestamo().')

doc.add_heading('6. Sesion', level=1)
doc.add_paragraph('SesionUsuario: object singleton con usuario en memoria (id/nombre/correo/rol). Usado para validar y filtrar.')

doc.add_heading('7. Flujo de solicitud (a grandes rasgos)', level=1)
doc.add_paragraph('1. Usuario pulsa Solicitar -> dialogo confirma -> ViewModel.solicitarPrestamo')
doc.add_paragraph('2. Repo valida sesion/duplicados -> intenta API')
doc.add_paragraph('3. Si OK -> Confirmado. Si 409/404 -> estados especificos. Si red falla -> guarda offline (GuardadoSinConexion)')
doc.add_paragraph('4. UI muestra mensaje/toast/snackbar segun estado')
doc.add_paragraph('5. Al reconectar, SyncManager reintenta y actualiza estado en Room')

doc.save('E:/API/Explicacion_Codigo_Grandes_Razgos_Bibliotecnm.docx')
print('done')
