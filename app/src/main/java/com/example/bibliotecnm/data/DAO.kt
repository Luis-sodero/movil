package com.example.bibliotecnm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestamoPendienteDao {
    @Insert
    suspend fun insert(prestamo: PrestamoPendiente): Long

    @Query("SELECT * FROM prestamosPendientes WHERE estado = 'PENDIENTE' ORDER BY timestampClic ASC")
    fun getPendientes(): Flow<List<PrestamoPendiente>>

    @Query("SELECT * FROM prestamosPendientes WHERE estado = 'PENDIENTE' ORDER BY timestampClic ASC")
    suspend fun getPendientesList(): List<PrestamoPendiente>

    @Query("UPDATE prestamosPendientes SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)

    @Query("SELECT * FROM prestamosPendientes WHERE estado = 'RECHAZADO_SIN_EJEMPLARES' AND avisado = 0")
    fun getRechazadosSinAvisar(): Flow<List<PrestamoPendiente>>

    @Query("UPDATE prestamosPendientes SET avisado = 1 WHERE id = :id")
    suspend fun marcarAvisado(id: Long)

    @Query("DELETE FROM prestamosPendientes WHERE id = :id")
    suspend fun eliminar(id: Long)
}