package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.DataBaseLocal
import com.example.bibliotecnm.data.PrestamoDto
import com.example.bibliotecnm.data.PrestamoRepository
import com.example.bibliotecnm.data.ResultadoPrestamo
import com.example.bibliotecnm.data.RetrofitClient
import com.example.bibliotecnm.data.SesionUsuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class PrestamosUiState {
    object Cargando : PrestamosUiState()
    data class Exito(val prestamos: List<PrestamoDto>) : PrestamosUiState()
    data class Error(val mensaje: String) : PrestamosUiState()
}

class PrestamosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PrestamoRepository by lazy {
        val db = DataBaseLocal.getDatabase(application)
        val dao = db.prestamoPendienteDao()
        val apiService = RetrofitClient.apiService
        PrestamoRepository(dao, apiService)
    }

    private val _uiState = MutableStateFlow<PrestamosUiState>(PrestamosUiState.Cargando)
    val uiState: StateFlow<PrestamosUiState> = _uiState.asStateFlow()

    private val _resultadoPrestamo = MutableSharedFlow<ResultadoPrestamo>()
    val resultadoPrestamo: SharedFlow<ResultadoPrestamo> = _resultadoPrestamo.asSharedFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    fun getRechazadosSinAvisar() = repository.getRechazadosSinAvisar()

    suspend fun marcarAvisado(id: Long) = repository.marcarAvisado(id)

    init {
        cargarPrestamos(mostrarCargando = true)
    }

    fun cargarPrestamos(usuarioId: Int = SesionUsuario.id, mostrarCargando: Boolean = true) {
        viewModelScope.launch {
            val estadoActual = _uiState.value
            if (mostrarCargando && estadoActual !is PrestamosUiState.Exito) {
                _uiState.value = PrestamosUiState.Cargando
            }
            if (!mostrarCargando) {
                _refrescando.value = true
            }
            val result = repository.obtenerMisPrestamos(usuarioId)
            result.onSuccess { prestamos ->
                _uiState.value = PrestamosUiState.Exito(prestamos)
            }.onFailure { error ->
                if (error is IOException && estadoActual is PrestamosUiState.Exito) {
                    // conservar
                } else if (error is IOException) {
                    _uiState.value = PrestamosUiState.Error("Sin conexión, mostrando datos guardados")
                } else {
                    if (!(error is IOException && estadoActual is PrestamosUiState.Exito)) {
                        _uiState.value = PrestamosUiState.Error(error.message ?: "Error de conexión")
                    }
                }
            }
            if (!mostrarCargando) {
                _refrescando.value = false
            }
        }
    }

    fun pedirPrestamo(libroId: Int, titulo: String) {
        viewModelScope.launch {
            val res = repository.pedirPrestamo(libroId, titulo)
            _resultadoPrestamo.emit(res)
            if (res is com.example.bibliotecnm.data.ResultadoPrestamo.Confirmado) {
                cargarPrestamos()
            }
        }
    }

    fun guardarPrestamo(libroId: Int, titulo: String, usuarioId: Int = SesionUsuario.id) {
        viewModelScope.launch {
            pedirPrestamo(libroId, titulo)
        }
    }
}
