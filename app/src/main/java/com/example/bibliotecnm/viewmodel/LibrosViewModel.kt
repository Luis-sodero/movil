package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.DataBaseLocal
import com.example.bibliotecnm.data.LibroVistaDto
import com.example.bibliotecnm.data.PrestamoRepository
import com.example.bibliotecnm.data.ResultadoPrestamo
import com.example.bibliotecnm.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class LibrosUiState {
    object Cargando : LibrosUiState()
    data class Exito(val libros: List<LibroVistaDto>) : LibrosUiState()
    data class Error(val mensaje: String) : LibrosUiState()
}

sealed class SolicitudUiState {
    object Idle : SolicitudUiState()
    object Cargando : SolicitudUiState()
    data class Exito(val mensaje: String) : SolicitudUiState()
    object GuardadoSinConexion : SolicitudUiState()
    data class Error(val mensaje: String) : SolicitudUiState()
}

class LibrosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PrestamoRepository by lazy {
        val db = DataBaseLocal.getDatabase(application)
        val dao = db.prestamoPendienteDao()
        val apiService = RetrofitClient.apiService
        PrestamoRepository(dao, apiService)
    }

    private val _uiState = MutableStateFlow<LibrosUiState>(LibrosUiState.Cargando)
    val uiState: StateFlow<LibrosUiState> = _uiState.asStateFlow()

    private val _solicitudState = MutableStateFlow<SolicitudUiState>(SolicitudUiState.Idle)
    val solicitudState: StateFlow<SolicitudUiState> = _solicitudState.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    init {
        cargarLibros(mostrarCargando = true)
    }

    fun cargarLibros(mostrarCargando: Boolean = true) {
        viewModelScope.launch {
            val estadoActual = _uiState.value
            if (mostrarCargando && estadoActual !is LibrosUiState.Exito) {
                _uiState.value = LibrosUiState.Cargando
            }
            if (!mostrarCargando) {
                _refrescando.value = true
            }
            val result = repository.obtenerLibros()
            result.onSuccess { libros ->
                _uiState.value = LibrosUiState.Exito(libros)
            }.onFailure { error ->
                if (error is IOException && estadoActual is LibrosUiState.Exito) {
                    // conservar datos, no cambiar a error
                } else if (error is IOException) {
                    _uiState.value = LibrosUiState.Error("Sin conexión, mostrando datos guardados")
                } else {
                    if (!(error is IOException && estadoActual is LibrosUiState.Exito)) {
                        _uiState.value = LibrosUiState.Error(error.message ?: "Error de conexión")
                    }
                }
            }
            if (!mostrarCargando) {
                _refrescando.value = false
            }
        }
    }

    fun solicitarPrestamo(libroId: Int, titulo: String) {
        viewModelScope.launch {
            _solicitudState.value = SolicitudUiState.Cargando
            val res = repository.pedirPrestamo(libroId, titulo)
            when (res) {
                is ResultadoPrestamo.Confirmado -> {
                    _solicitudState.value = SolicitudUiState.Exito("Préstamo solicitado con éxito")
                    cargarLibros(mostrarCargando = false)
                }
                is ResultadoPrestamo.GuardadoSinConexion -> {
                    _solicitudState.value = SolicitudUiState.GuardadoSinConexion
                }
                is ResultadoPrestamo.SinEjemplares -> {
                    _solicitudState.value = SolicitudUiState.Error("No hay ejemplares libres")
                }
                is ResultadoPrestamo.UsuarioNoEncontrado -> {
                    _solicitudState.value = SolicitudUiState.Error("Usuario no encontrado")
                }
                is ResultadoPrestamo.SesionInvalida -> {
                    _solicitudState.value = SolicitudUiState.Error("Sesión no válida. Vuelve a iniciar sesión.")
                }
                is ResultadoPrestamo.ErrorDesconocido -> {
                    _solicitudState.value = SolicitudUiState.Error(res.mensaje)
                }
            }
        }
    }

    fun resetSolicitudState() {
        _solicitudState.value = SolicitudUiState.Idle
    }
}
