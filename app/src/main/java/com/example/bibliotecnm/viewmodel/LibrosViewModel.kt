package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.DataBaseLocal
import com.example.bibliotecnm.data.ErrorApp
import com.example.bibliotecnm.data.LibroVistaDto
import com.example.bibliotecnm.data.PrestamoRepository
import com.example.bibliotecnm.data.ResultadoPrestamo
import com.example.bibliotecnm.data.RetrofitClient
import com.example.bibliotecnm.data.aDto
import com.example.bibliotecnm.data.aEntidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class LibrosUiState {
    object Cargando : LibrosUiState()
    data class Exito(val libros: List<LibroVistaDto>) : LibrosUiState()
    data class Error(val codigo: ErrorApp) : LibrosUiState()
}

sealed class SolicitudUiState {
    object Idle : SolicitudUiState()
    object Cargando : SolicitudUiState()
    object Exito : SolicitudUiState()
    object GuardadoSinConexion : SolicitudUiState()
    data class Error(val codigo: ErrorApp) : SolicitudUiState()
}

class LibrosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PrestamoRepository by lazy {
        val db = DataBaseLocal.getDatabase(application)
        val dao = db.prestamoPendienteDao()
        val apiService = RetrofitClient.apiService
        PrestamoRepository(dao, apiService)
    }

    private val libroDao by lazy { DataBaseLocal.getDatabase(application).libroDao() }

    private val _uiState = MutableStateFlow<LibrosUiState>(LibrosUiState.Cargando)
    val uiState: StateFlow<LibrosUiState> = _uiState.asStateFlow()

    private val _solicitudState = MutableStateFlow<SolicitudUiState>(SolicitudUiState.Idle)
    val solicitudState: StateFlow<SolicitudUiState> = _solicitudState.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private val _aviso = MutableStateFlow<ErrorApp?>(null)
    val aviso: StateFlow<ErrorApp?> = _aviso.asStateFlow()

    init {
        viewModelScope.launch {
            libroDao.observar().collect { guardados ->
                val libros = guardados.map { it.aDto() }
                if (libros.isNotEmpty() && _uiState.value !is LibrosUiState.Exito) {
                    _uiState.value = LibrosUiState.Exito(libros)
                }
            }
        }
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
                libroDao.guardar(libros.map { it.aEntidad() })
                _aviso.value = null
                _uiState.value = LibrosUiState.Exito(libros)
            }.onFailure { error ->
                if (_uiState.value is LibrosUiState.Exito) {
                    _aviso.value = if (error is IOException) {
                        ErrorApp.AVISO_SIN_CONEXION
                    } else {
                        ErrorApp.AVISO_NO_ACTUALIZADO
                    }
                } else {
                    _uiState.value = LibrosUiState.Error(
                        if (error is IOException) ErrorApp.AVISO_SIN_CONEXION else ErrorApp.AVISO_NO_ACTUALIZADO
                    )
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
                    _solicitudState.value = SolicitudUiState.Exito
                    cargarLibros(mostrarCargando = false)
                }
                is ResultadoPrestamo.GuardadoSinConexion -> {
                    _solicitudState.value = SolicitudUiState.GuardadoSinConexion
                }
                is ResultadoPrestamo.SinEjemplares -> {
                    _solicitudState.value = SolicitudUiState.Error(ErrorApp.SIN_EJEMPLARES)
                }
                is ResultadoPrestamo.UsuarioNoEncontrado -> {
                    _solicitudState.value = SolicitudUiState.Error(ErrorApp.USUARIO_NO_ENCONTRADO)
                }
                is ResultadoPrestamo.SesionInvalida -> {
                    _solicitudState.value = SolicitudUiState.Error(ErrorApp.SESION_INVALIDA)
                }
                is ResultadoPrestamo.ErrorDesconocido -> {
                    _solicitudState.value = SolicitudUiState.Error(res.codigo)
                }
            }
        }
    }

    fun resetSolicitudState() {
        _solicitudState.value = SolicitudUiState.Idle
    }
}
