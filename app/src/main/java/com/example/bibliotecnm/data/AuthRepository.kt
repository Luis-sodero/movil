package com.example.bibliotecnm.data

import com.google.gson.Gson

class AuthRepository {
    private val apiService = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(correo.trim(), password))

            if (!response.isSuccessful) {
                val mensaje = response.errorBody()?.string()?.let { body ->
                    runCatching { gson.fromJson(body, MensajeDto::class.java)?.mensaje }.getOrNull()
                }

                val texto = when (response.code()) {
                    401 -> mensaje ?: "Correo o contraseña incorrectos."
                    else -> mensaje ?: "No se pudo iniciar sesión (${response.code()})."
                }

                return Result.failure(Exception(texto))
            }

            val usuario = response.body()
            if (usuario?.id == null) {
                Result.failure(Exception("Respuesta inválida del servidor."))
            } else {
                Result.success(usuario)
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo conectar con la API. Verifica que esté en ejecución."))
        }
    }
}
