package com.example.bibliotecnm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.LibroVistaDto
import com.example.bibliotecnm.data.PrestamoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LibrosUiState {
    object Cargando : LibrosUiState()
    data class Exito(val libros: List<LibroVistaDto>) : LibrosUiState()
    data class Error(val mensaje: String) : LibrosUiState()
}

sealed class SolicitudUiState {
    object Idle : SolicitudUiState()
    object Cargando : SolicitudUiState()
    data class Exito(val mensaje: String) : SolicitudUiState()
    data class Error(val mensaje: String) : SolicitudUiState()
}

class LibrosViewModel : ViewModel() {
    private val repository = PrestamoRepository()

    private val _uiState = MutableStateFlow<LibrosUiState>(LibrosUiState.Cargando)
    val uiState: StateFlow<LibrosUiState> = _uiState.asStateFlow()

    private val _solicitudState = MutableStateFlow<SolicitudUiState>(SolicitudUiState.Idle)
    val solicitudState: StateFlow<SolicitudUiState> = _solicitudState.asStateFlow()

    init {
        cargarLibros()
    }

    fun cargarLibros() {
        viewModelScope.launch {
            _uiState.value = LibrosUiState.Cargando
            val result = repository.obtenerLibros()
            result.onSuccess { libros ->
                _uiState.value = LibrosUiState.Exito(libros)
            }.onFailure { error ->
                _uiState.value = LibrosUiState.Error(error.message ?: "Error de conexión")
            }
        }
    }

    fun solicitarPrestamo(libroId: Int) {
        viewModelScope.launch {
            _solicitudState.value = SolicitudUiState.Cargando
            val result = repository.solicitarPrestamo(libroId)
            result.onSuccess {
                _solicitudState.value = SolicitudUiState.Exito("Préstamo solicitado con éxito")
                cargarLibros()
            }.onFailure { error ->
                _solicitudState.value = SolicitudUiState.Error(error.message ?: "No disponible")
            }
        }
    }

    fun resetSolicitudState() {
        _solicitudState.value = SolicitudUiState.Idle
    }
}
