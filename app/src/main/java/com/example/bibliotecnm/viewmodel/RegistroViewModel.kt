package com.example.bibliotecnm.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.AuthRepository
import com.example.bibliotecnm.data.ErrorApp
import com.example.bibliotecnm.data.ErrorAppException
import com.example.bibliotecnm.data.LoginResponse
import com.example.bibliotecnm.data.SesionUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistroUiState {
    object Inactivo : RegistroUiState()
    object Cargando : RegistroUiState()
    data class Exito(val usuario: LoginResponse) : RegistroUiState()
    data class Error(val codigo: ErrorApp) : RegistroUiState()
}

class RegistroViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Inactivo)
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun registrar(nombre: String, correo: String, password: String) {
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()

        if (nombreLimpio.isBlank() || correoLimpio.isBlank() || password.isBlank()) {
            _uiState.value = RegistroUiState.Error(ErrorApp.CAMPOS_REGISTRO)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            _uiState.value = RegistroUiState.Error(ErrorApp.CORREO_INVALIDO)
            return
        }

        if (password.length < 6) {
            _uiState.value = RegistroUiState.Error(ErrorApp.CONTRASENA_CORTA)
            return
        }

        viewModelScope.launch {
            _uiState.value = RegistroUiState.Cargando

            repository.registrar(nombreLimpio, correoLimpio, password)
                .onSuccess { usuario ->
                    SesionUsuario.iniciar(getApplication(), usuario)
                    _uiState.value = RegistroUiState.Exito(usuario)
                }
                .onFailure { error ->
                    val codigo = (error as? ErrorAppException)?.codigo ?: ErrorApp.DESCONOCIDO
                    _uiState.value = RegistroUiState.Error(codigo)
                }
        }
    }

    fun limpiarError() {
        if (_uiState.value is RegistroUiState.Error) {
            _uiState.value = RegistroUiState.Inactivo
        }
    }
}
