package com.example.bibliotecnm.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("correo")
    val correo: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("correo")
    val correo: String? = null,
    @SerializedName("rol")
    val rol: String? = null
)

data class LibroVistaDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("titulo")
    val titulo: String? = null,
    @SerializedName("anio")
    val anio: Int? = null,
    @SerializedName("autores")
    val autores: String? = null,
    @SerializedName("generos")
    val generos: String? = null,
    @SerializedName("ejemplaresTotal")
    val ejemplaresTotal: Int? = null,
    @SerializedName("ejemplaresDisponibles")
    val ejemplaresDisponibles: Int? = null
)

data class PrestamoDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("titulo")
    val titulo: String? = null,
    @SerializedName("autor")
    val autor: String? = null,
    @SerializedName("fechaLimite")
    val fechaLimite: String? = null,
    @SerializedName("fechaDevolucion")
    val fechaDevolucion: String? = null
)

data class SolicitudPrestamoRequest(
    @SerializedName("libroId")
    val libroId: Int,
    @SerializedName("usuarioId")
    val usuarioId: Int,
    @SerializedName("timestampSolicitud")
    val timestampSolicitud: Long = System.currentTimeMillis()
)

data class SolicitudPrestamoResponse(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("ejemplarId")
    val ejemplarId: Int? = null,
    @SerializedName("fechaLimite")
    val fechaLimite: String? = null
)

data class MensajeDto(
    @SerializedName("mensaje")
    val mensaje: String? = null,
    @SerializedName("codigo")
    val codigo: String? = null
)

sealed class ResultadoPrestamo {
    data class Confirmado(val respuesta: SolicitudPrestamoResponse) : ResultadoPrestamo()
    object GuardadoSinConexion : ResultadoPrestamo()
    object SinEjemplares : ResultadoPrestamo()
    object UsuarioNoEncontrado : ResultadoPrestamo()
    object SesionInvalida : ResultadoPrestamo()
    data class ErrorDesconocido(val mensaje: String) : ResultadoPrestamo()
}
