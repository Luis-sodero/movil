# requiere python-docx
python -c @"
# -*- coding: utf-8 -*-
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from datetime import datetime
doc = Document()
doc.add_heading('Documento de Requerimientos y Arquitectura', 0).alignment = WD_ALIGN_PARAGRAPH.CENTER
doc.add_paragraph('Proyecto: Bibliotecnm')
doc.add_paragraph('Version: 1.0')
doc.add_paragraph('Fecha: ' + datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
doc.add_paragraph('Estado: En analisis / Parcial')
doc.add_page_break()
doc.add_heading('1. Introduccion', level=1)
doc.add_paragraph('API .NET + App Android + BD PostgreSQL')
doc.add_heading('2. Alcance', level=1)
doc.add_paragraph('Implementado: catalogo, login parcial, prestamos, sincronizacion parcial. Propuesto: devoluciones, auth robusta.')
doc.add_heading('3. Arquitectura', level=1)
doc.add_paragraph('MVVM movil, API REST .NET, PostgreSQL')
doc.add_heading('4. Requerimientos', level=1)
doc.add_paragraph('RF-001 Login (parcial), RF-002 Catalogo (implementado), RF-003 Solicitar prestamo (parcial), RF-004 Mis prestamos (implementado)')
doc.add_heading('5. BD', level=1)
doc.add_paragraph('usuarios, libros, autores, generos, libros_autores, libros_generos, ejemplares, prestamos, vista_libros')
doc.add_heading('6. Pendientes', level=1)
doc.add_paragraph('Completar devoluciones, auth, notificaciones, pruebas')
doc.save('E:/API/Documento_Requerimientos_Arquitectura.docx')
print('done')
"@
