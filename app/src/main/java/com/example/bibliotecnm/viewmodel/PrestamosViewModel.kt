package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.DataBaseLocal
import com.example.bibliotecnm.data.PrestamoDto
import com.example.bibliotecnm.data.PrestamoPendiente
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

    fun getRechazadosSinAvisar() = repository.getRechazadosSinAvisar()

    suspend fun marcarAvisado(id: Long) = repository.marcarAvisado(id)

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
