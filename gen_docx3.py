from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from datetime import datetime

doc = Document()
title = doc.add_heading('Documentacion Tecnica - App Movil Bibliotecnm', 0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('Generado: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Proyecto: E:\\API\\Bibliotecnm')
doc.add_paragraph('Paquete: com.example.bibliotecnm')
doc.add_paragraph('')

doc.add_heading('1. Objetivo', level=1)
doc.add_paragraph('Documentar arquitectura, navegacion, pantallas, ViewModels, capa de datos, sincronizacion offline y ubicacion de cada archivo funcional.')

doc.add_heading('2. Arquitectura', level=1)
doc.add_paragraph('MVVM con Jetpack Compose, Navigation, Retrofit, Room, Coroutines/Flow.')

doc.add_heading('3. Archivos funcionales', level=1)
doc.add_heading('3.1 Raiz', level=2)
doc.add_paragraph('MainActivity.kt - Inicializa DB/Repo/SyncManager, registra conectividad, muestra AppNavigation')
doc.add_paragraph('AppNavigation.kt - NavHost (LOGIN/PRINCIPAL/PERFIL), inicio segun sesion')
doc.add_paragraph('PerfilActivity.kt - Perfil y lista de prestamos')

doc.add_heading('3.2 ui/', level=2)
doc.add_paragraph('LoginScreen.kt - Pantalla login')
doc.add_paragraph('theme/ - Tema Material3')

doc.add_heading('3.3 viewmodel/', level=2)
doc.add_paragraph('LoginViewModel.kt - Autenticacion')
doc.add_paragraph('LibrosViewModel.kt - Catalogo y solicitudes')
doc.add_paragraph('PrestamosViewModel.kt - Prestamos usuario')

doc.add_heading('3.4 data/', level=2)
doc.add_paragraph('ApiModels.kt - DTOs y ResultadoPrestamo')
doc.add_paragraph('ApiService.kt - Endpoints')
doc.add_paragraph('RetrofitClient.kt - Cliente HTTP (BASE_URL ngrok)')
doc.add_paragraph('AuthRepository.kt - Login')
doc.add_paragraph('PrestamoRepository.kt - Libros/prestamos + offline/sync')
doc.add_paragraph('Entity.kt, DAO.kt, DataBaseLocal.kt - Room')
doc.add_paragraph('SesionUsuario.kt - Sesion en memoria')
doc.add_paragraph('SyncManager.kt - Sincronizacion')

doc.add_heading('4. Flujo', level=1)
doc.add_paragraph('Login -> Principal (limpia stack). Principal <-> Perfil. Cerrar sesion -> Login.')

doc.add_heading('5. Offline', level=1)
doc.add_paragraph('Al solicitar sin red se guarda en Room (PENDIENTE). SyncManager sincroniza al reconectar.')

doc.add_heading('6. Endpoints', level=1)
doc.add_paragraph('POST api/Auth/login')
doc.add_paragraph('GET api/VistaLibros')
doc.add_paragraph('GET api/Prestamos?usuarioId=')
doc.add_paragraph('POST api/Prestamos/solicitar')

doc.save('E:/API/Documentacion_App_Movil_Bibliotecnm.docx')
print('done')
