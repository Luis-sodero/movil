package com.example.bibliotecnm.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/registro")
    suspend fun registrar(@Body request: RegistroRequest): Response<LoginResponse>

    @GET("api/VistaLibros")
    suspend fun getVistaLibros(): List<LibroVistaDto>

    @GET("api/Prestamos")
    suspend fun getPrestamos(@Query("usuarioId") usuarioId: Int = 1): List<PrestamoDto>

    @POST("api/Prestamos/solicitar")
    suspend fun solicitarPrestamo(@Body request: SolicitudPrestamoRequest): Response<SolicitudPrestamoResponse>

    @Streaming
    @GET
    suspend fun descargarPdf(@Url url: String): ResponseBody
}
