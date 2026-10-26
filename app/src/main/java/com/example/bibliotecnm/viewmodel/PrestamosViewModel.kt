package com.example.bibliotecnm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.PrestamoDto
import com.example.bibliotecnm.data.PrestamoRepository
import com.example.bibliotecnm.data.SesionUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PrestamosUiState {
    object Cargando : PrestamosUiState()
    data class Exito(val prestamos: List<PrestamoDto>) : PrestamosUiState()
    data class Error(val mensaje: String) : PrestamosUiState()
}

class PrestamosViewModel : ViewModel() {
    private val repository = PrestamoRepository()

    private val _uiState = MutableStateFlow<PrestamosUiState>(PrestamosUiState.Cargando)
    val uiState: StateFlow<PrestamosUiState> = _uiState.asStateFlow()

    init {
        cargarPrestamos()
    }

    fun cargarPrestamos(usuarioId: Int = SesionUsuario.id) {
        viewModelScope.launch {
            _uiState.value = PrestamosUiState.Cargando
            val result = repository.obtenerMisPrestamos(usuarioId)
            result.onSuccess { prestamos ->
                _uiState.value = PrestamosUiState.Exito(prestamos)
            }.onFailure { error ->
                _uiState.value = PrestamosUiState.Error(error.message ?: "Error de conexión")
            }
        }
    }
}
