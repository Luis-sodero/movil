package com.example.bibliotecnm.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

open class PrestamoRepository(
    private val dao: PrestamoPendienteDao? = null,
    private val apiService: ApiService = RetrofitClient.apiService
) {
    private val syncMutex = Mutex()

    suspend fun guardarPrestamoOffline(libroId: Int, titulo: String, usuarioId: Int, timestampClic: Long = System.currentTimeMillis()): PrestamoPendiente {
        if (dao == null) {
            throw UnsupportedOperationException("Use constructor with dao")
        }
        val base = PrestamoPendiente(
            libroId = libroId,
            libroTitulo = titulo,
            usuarioId = usuarioId,
            timestampClic = timestampClic,
            estado = "PENDIENTE",
            avisado = false
        )
        val id = dao.insert(base)
        return base.copy(id = id)
    }

    suspend fun pendientes(): List<PrestamoPendiente> = dao?.getPendientesList() ?: emptyList()

    fun observarPendientes(): Flow<List<PrestamoPendiente>>? = dao?.getPendientes()

    fun getRechazadosSinAvisar(): Flow<List<PrestamoPendiente>>? = dao?.getRechazadosSinAvisar()

    suspend fun marcarAvisado(id: Long) {
        dao?.marcarAvisado(id)
    }

    suspend fun actualizarEstado(id: Long, estado: String) {
        dao?.actualizarEstado(id, estado)
    }

    suspend fun pedirPrestamo(libroId: Int, titulo: String): ResultadoPrestamo {
        val usuarioId = SesionUsuario.id
        if (usuarioId <= 0) {
            return ResultadoPrestamo.SesionInvalida
        }
        val timestamp = System.currentTimeMillis()
        return try {
            val resp = apiService.solicitarPrestamo(
                SolicitudPrestamoRequest(
                    libroId = libroId,
                    usuarioId = usuarioId,
                    timestampSolicitud = timestamp
                )
            )
            if (resp.isSuccessful && resp.body() != null) {
                ResultadoPrestamo.Confirmado(resp.body()!!)
            } else {
                when (resp.code()) {
                    409 -> ResultadoPrestamo.SinEjemplares
                    404 -> ResultadoPrestamo.UsuarioNoEncontrado
                    else -> ResultadoPrestamo.ErrorDesconocido("Error ${resp.code()}")
                }
            }
        } catch (e: IOException) {
            try {
                guardarPrestamoOffline(libroId, titulo, usuarioId, timestamp)
            } catch (_: Exception) {
            }
            ResultadoPrestamo.GuardadoSinConexion
        } catch (e: Exception) {
            ResultadoPrestamo.ErrorDesconocido(e.message ?: "Error desconocido")
        }
    }

    suspend fun sincronizarPrestamo(p: PrestamoPendiente): Result<Unit> = syncMutex.withLock {
        try {
            val resp = apiService.solicitarPrestamo(
                SolicitudPrestamoRequest(
                    libroId = p.libroId,
                    usuarioId = p.usuarioId,
                    timestampSolicitud = p.timestampClic
                )
            )
            if (resp.isSuccessful) {
                dao?.actualizarEstado(p.id, "SINCRONIZADO")
                Result.success(Unit)
            } else {
                when (resp.code()) {
                    409 -> {
                        dao?.actualizarEstado(p.id, "RECHAZADO_SIN_EJEMPLARES")
                        Result.failure(Exception("Sin ejemplares"))
                    }
                    404 -> {
                        dao?.actualizarEstado(p.id, "ERROR")
                        Result.failure(Exception("Usuario no encontrado"))
                    }
                    else -> Result.failure(Exception("Error ${resp.code()}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
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

    suspend fun obtenerMisPrestamos(usuarioId: Int = SesionUsuario.id): Result<List<PrestamoDto>> {
        return try {
            val prestamos = apiService.getPrestamos(usuarioId)
            Result.success(prestamos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerLibros(): Result<List<LibroVistaDto>> {
        return try {
            val libros = apiService.getVistaLibros()
            Result.success(libros)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
