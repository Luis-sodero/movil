package com.example.bibliotecnm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.AuthRepository
import com.example.bibliotecnm.data.LoginResponse
import com.example.bibliotecnm.data.SesionUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Inactivo : LoginUiState()
    object Cargando : LoginUiState()
    data class Exito(val usuario: LoginResponse) : LoginUiState()
    data class Error(val mensaje: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Inactivo)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Ingresa tu correo y contraseña.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Cargando

            repository.login(correo, password)
                .onSuccess { usuario ->
                    SesionUsuario.iniciar(usuario)
                    _uiState.value = LoginUiState.Exito(usuario)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "No se pudo iniciar sesión."
                    )
                }
        }
    }

    fun limpiarError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Inactivo
        }
    }
}
