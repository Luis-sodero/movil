package com.example.bibliotecnm.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.AuthRepository
import com.example.bibliotecnm.data.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistroUiState {
    object Inactivo : RegistroUiState()
    object Cargando : RegistroUiState()
    data class Exito(val usuario: LoginResponse) : RegistroUiState()
    data class Error(val mensaje: String) : RegistroUiState()
}

class RegistroViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Inactivo)
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun registrar(nombre: String, correo: String, password: String) {
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()

        if (nombreLimpio.isBlank() || correoLimpio.isBlank() || password.isBlank()) {
            _uiState.value = RegistroUiState.Error("Completa todos los campos.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            _uiState.value = RegistroUiState.Error("Ingresa un correo electrónico válido.")
            return
        }

        if (password.length < 6) {
            _uiState.value = RegistroUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegistroUiState.Cargando

            repository.registrar(nombreLimpio, correoLimpio, password)
                .onSuccess { usuario ->
                    _uiState.value = RegistroUiState.Exito(usuario)
                }
                .onFailure { error ->
                    _uiState.value = RegistroUiState.Error(
                        error.message ?: "No se pudo registrar la cuenta."
                    )
                }
        }
    }

    fun limpiarError() {
        if (_uiState.value is RegistroUiState.Error) {
            _uiState.value = RegistroUiState.Inactivo
        }
    }
}
