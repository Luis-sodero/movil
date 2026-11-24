from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from datetime import datetime

doc = Document()
title = doc.add_heading('Documentacion Tecnica - App Movil Bibliotecnm (Android/Kotlin + Jetpack Compose)', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph(f'Generado: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}')
doc.add_paragraph('Ruta del proyecto: E:\\API\\Bibliotecnm')
doc.add_paragraph('')

doc.add_heading('1. Objetivo', level=1)
doc.add_paragraph('Documentar el funcionamiento completo de la aplicacion movil Bibliotecnm: arquitectura, navegacion, pantallas, ViewModels, capa de datos (API, Room, sincronizacion offline), y ubicacion de cada archivo funcional con su proposito.')

doc.add_heading('2. Arquitectura General', level=1)
doc.add_paragraph('Arquitectura MVVM con Jetpack Compose, Navigation Component, Retrofit, Room, Coroutines/Flow. Paquete base: com.example.bibliotecnm')

doc.add_heading('3. Estructura de Archivos Funcionales', level=1)
doc.add_heading('3.1 Raiz', level=2)
doc.add_paragraph('MainActivity.kt - Inicializa DB/Repo/SyncManager, edge-to-edge, AppNavigation')
doc.add_paragraph('AppNavigation.kt - NavHost con rutas LOGIN/PRINCIPAL/PERFIL, inicio segun sesion')
doc.add_paragraph('PerfilActivity.kt - Pantalla de perfil y prestamos')

doc.add_heading('3.2 ui/', level=2)
doc.add_paragraph('ui/LoginScreen.kt - Pantalla de login')
doc.add_paragraph('ui/theme/ - Tema Material3')

doc.add_heading('3.3 viewmodel/', level=2)
doc.add_paragraph('viewmodel/LoginViewModel.kt - Login')
doc.add_paragraph('viewmodel/LibrosViewModel.kt - Catalogo y solicitud de prestamos')
doc.add_paragraph('viewmodel/PrestamosViewModel.kt - Prestamos del usuario')

doc.add_heading('3.4 data/', level=2)
doc.add_paragraph('data/ApiModels.kt - DTOs y ResultadoPrestamo')
doc.add_paragraph('data/ApiService.kt - Endpoints Retrofit')
doc.add_paragraph('data/RetrofitClient.kt - Cliente HTTP')
doc.add_paragraph('data/AuthRepository.kt - Autenticacion')
doc.add_paragraph('data/PrestamoRepository.kt - Prestamos/libros + offline')
doc.add_paragraph('data/Entity.kt - PrestamoPendiente (Room)')
doc.add_paragraph('data/DAO.kt - PrestamoPendienteDao')
doc.add_paragraph('data/DataBaseLocal.kt - Room DB')
doc.add_paragraph('data/SesionUsuario.kt - Sesion en memoria')
doc.add_paragraph('data/SyncManager.kt - Sincronizacion al conectar')

doc.add_heading('4. Navegacion', level=1)
doc.add_paragraph('LOGIN -> PRINCIPAL (limpia backstack). PRINCIPAL -> PERFIL. PERFIL vuelve o cierra sesion (limpia stack).')

doc.add_heading('5. Pantallas', level=1)
doc.add_paragraph('PantallaLogin: formulario, validacion, LoginViewModel.')
doc.add_paragraph('PantallaPrincipal: catalogo, busqueda, solicitar con dialogo, recarga ON_RESUME/cada 30s, pull-to-refresh.')
doc.add_paragraph('PantallaPerfil: datos usuario, lista prestamos, menu config, cerrar sesion.')

doc.add_heading('6. Lógica Clave', level=1)
doc.add_paragraph('Auth: POST /api/Auth/login, guarda en SesionUsuario.')
doc.add_paragraph('Libros: GET /api/VistaLibros.')
doc.add_paragraph('Solicitud: POST /api/Prestamos/solicitar. Si sin red -> guarda en Room (PENDIENTE).')
doc.add_paragraph('Sync: al reconectar, SyncManager sincroniza pendientes (actualiza estado SINCRONIZADO/RECHAZADO_SIN_EJEMPLARES/ERROR).')

doc.add_heading('7. Configuracion', level=1)
doc.add_paragraph('BASE_URL: https://gorgeous-refract-provoking.ngrok-free.dev/')
doc.add_paragraph('Room: biblioteca_db v3')

doc.save('E:/API/Documentacion_App_Movil_Bibliotecnm.docx')
print('done')
