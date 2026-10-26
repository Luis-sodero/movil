package com.example.bibliotecnm.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/VistaLibros")
    suspend fun getVistaLibros(): List<LibroVistaDto>

    @GET("api/Prestamos")
    suspend fun getPrestamos(@Query("usuarioId") usuarioId: Int = 1): List<PrestamoDto>

    @POST("api/Prestamos/solicitar")
    suspend fun solicitarPrestamo(@Body request: SolicitudPrestamoRequest): Response<SolicitudPrestamoResponse>
}
