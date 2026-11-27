package com.example.bibliotecnm.ui

import com.example.bibliotecnm.R
import com.example.bibliotecnm.data.ErrorApp

fun ErrorApp.mensaje(): Int = when (this) {
    ErrorApp.CAMPOS_LOGIN -> R.string.error_campos_login
    ErrorApp.CAMPOS_REGISTRO -> R.string.error_campos_registro
    ErrorApp.CORREO_INVALIDO -> R.string.error_correo_invalido
    ErrorApp.CONTRASENA_CORTA -> R.string.error_contrasena_corta
    ErrorApp.CREDENCIALES_INVALIDAS -> R.string.error_credenciales
    ErrorApp.CORREO_YA_EXISTE -> R.string.error_correo_existe
    ErrorApp.DATOS_INVALIDOS -> R.string.error_datos_invalidos
    ErrorApp.RESPUESTA_INVALIDA -> R.string.error_respuesta
    ErrorApp.SIN_CONEXION -> R.string.error_sin_conexion
    ErrorApp.SERVIDOR -> R.string.error_servidor
    ErrorApp.SIN_ESPACIO -> R.string.error_sin_espacio
    ErrorApp.PDF_DANADO -> R.string.error_pdf_danado
    ErrorApp.PDF_NO_DISPONIBLE -> R.string.error_pdf_no_disponible
    ErrorApp.LECTURA_SIN_PDF -> R.string.error_lectura_sin_pdf
    ErrorApp.SIN_EJEMPLARES -> R.string.error_sin_ejemplares
    ErrorApp.USUARIO_NO_ENCONTRADO -> R.string.error_usuario_no_encontrado
    ErrorApp.SESION_INVALIDA -> R.string.error_sesion_invalida
    ErrorApp.SOLICITUD_PENDIENTE -> R.string.error_solicitud_pendiente
    ErrorApp.AVISO_SIN_CONEXION -> R.string.aviso_sin_conexion
    ErrorApp.AVISO_NO_ACTUALIZADO -> R.string.aviso_no_actualizado
    ErrorApp.DESCONOCIDO -> R.string.error_desconocido
}
