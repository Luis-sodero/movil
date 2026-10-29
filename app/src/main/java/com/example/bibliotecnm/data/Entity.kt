package com.example.bibliotecnm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prestamosPendientes")
data class PrestamoPendiente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libroId: Int,
    val libroTitulo: String,
    val usuarioId: Int,
    val timestampClic: Long,
    val estado: String = "PENDIENTE",
    val avisado: Boolean = false
)
