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

@Entity(tableName = "librosGuardados")
data class LibroGuardado(
    @PrimaryKey val id: Int,
    val titulo: String?,
    val anio: Int?,
    val autores: String?,
    val generos: String?,
    val urlPortada: String?,
    val urlPdf: String?,
    val ejemplaresTotal: Int?,
    val ejemplaresDisponibles: Int?
)

fun LibroVistaDto.aEntidad(): LibroGuardado = LibroGuardado(
    id = id ?: 0,
    titulo = titulo,
    anio = anio,
    autores = autores,
    generos = generos,
    urlPortada = urlPortada,
    urlPdf = urlPdf,
    ejemplaresTotal = ejemplaresTotal,
    ejemplaresDisponibles = ejemplaresDisponibles
)

fun LibroGuardado.aDto(): LibroVistaDto = LibroVistaDto(
    id = id,
    titulo = titulo,
    anio = anio,
    autores = autores,
    generos = generos,
    urlPortada = urlPortada,
    urlPdf = urlPdf,
    ejemplaresTotal = ejemplaresTotal,
    ejemplaresDisponibles = ejemplaresDisponibles
)
