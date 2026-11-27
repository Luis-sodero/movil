package com.example.bibliotecnm.data

import java.io.IOException

class AuthRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(correo.trim(), password))

            if (!response.isSuccessful) {
                val codigo = when (response.code()) {
                    401 -> ErrorApp.CREDENCIALES_INVALIDAS
                    400 -> ErrorApp.DATOS_INVALIDOS
                    else -> ErrorApp.SERVIDOR
                }
                return Result.failure(ErrorAppException(codigo))
            }

            val usuario = response.body()
            if (usuario?.id == null) {
                Result.failure(ErrorAppException(ErrorApp.RESPUESTA_INVALIDA))
            } else {
                Result.success(usuario)
            }
        } catch (e: IOException) {
            Result.failure(ErrorAppException(ErrorApp.SIN_CONEXION))
        } catch (e: Exception) {
            Result.failure(ErrorAppException(ErrorApp.SERVIDOR))
        }
    }

    suspend fun registrar(nombre: String, correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.registrar(RegistroRequest(nombre.trim(), correo.trim(), password))

            if (!response.isSuccessful) {
                val codigo = when (response.code()) {
                    409 -> ErrorApp.CORREO_YA_EXISTE
                    400 -> ErrorApp.DATOS_INVALIDOS
                    else -> ErrorApp.SERVIDOR
                }
                return Result.failure(ErrorAppException(codigo))
            }

            val usuario = response.body()
            if (usuario?.id == null) {
                Result.failure(ErrorAppException(ErrorApp.RESPUESTA_INVALIDA))
            } else {
                Result.success(usuario)
            }
        } catch (e: IOException) {
            Result.failure(ErrorAppException(ErrorApp.SIN_CONEXION))
        } catch (e: Exception) {
            Result.failure(ErrorAppException(ErrorApp.SERVIDOR))
        }
    }
}
