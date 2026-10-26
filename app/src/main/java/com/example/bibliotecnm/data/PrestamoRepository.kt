package com.example.bibliotecnm.data

import retrofit2.HttpException

class PrestamoRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun obtenerLibros(): Result<List<LibroVistaDto>> {
        return try {
            val libros = apiService.getVistaLibros()
            Result.success(libros)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMisPrestamos(usuarioId: Int = SesionUsuario.id): Result<List<PrestamoDto>> {
        if (usuarioId <= 0) {
            return Result.failure(Exception("Sesión no válida. Vuelve a iniciar sesión."))
        }

        return try {
            val prestamos = apiService.getPrestamos(usuarioId)
            Result.success(prestamos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solicitarPrestamo(libroId: Int): Result<SolicitudPrestamoResponse> {
        val usuarioId = SesionUsuario.id
        if (usuarioId <= 0) {
            return Result.failure(Exception("Sesión no válida. Vuelve a iniciar sesión."))
        }

        return try {
            val response = apiService.solicitarPrestamo(
                SolicitudPrestamoRequest(libroId = libroId, usuarioId = usuarioId)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                when (response.code()) {
                    409 -> Result.failure(Exception("No hay ejemplares libres"))
                    404 -> Result.failure(Exception("Usuario no encontrado"))
                    else -> Result.failure(Exception("Error al solicitar préstamo"))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Result.failure(Exception("No hay ejemplares libres"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
