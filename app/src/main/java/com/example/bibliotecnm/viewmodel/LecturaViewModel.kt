package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.ErrorApp
import com.example.bibliotecnm.data.PdfStorage
import com.example.bibliotecnm.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LecturaViewModel(application: Application) : AndroidViewModel(application) {
    sealed class LecturaUiState {
        object Cargando : LecturaUiState()
        data class Listo(val ruta: String) : LecturaUiState()
        data class Error(val codigo: ErrorApp) : LecturaUiState()
    }

    private val _uiState = MutableStateFlow<LecturaUiState>(LecturaUiState.Cargando)
    val uiState: StateFlow<LecturaUiState> = _uiState.asStateFlow()

    fun preparar(libroId: Int, urlPdf: String?) {
        viewModelScope.launch {
            _uiState.value = LecturaUiState.Cargando

            val descarga = PdfStorage.archivoDescarga(getApplication(), libroId)
            if (descarga.exists()) {
                _uiState.value = LecturaUiState.Listo(descarga.path)
                return@launch
            }

            val url = RetrofitClient.urlPdf(urlPdf)
            if (url.isNullOrBlank()) {
                _uiState.value = LecturaUiState.Error(ErrorApp.LECTURA_SIN_PDF)
                return@launch
            }

            try {
                PdfStorage.descargar(
                    url = url,
                    destino = PdfStorage.archivoLectura(getApplication(), libroId),
                    temporal = PdfStorage.archivoTemporalLectura(getApplication(), libroId),
                    onProgreso = {}
                )
                _uiState.value = LecturaUiState.Listo(
                    PdfStorage.archivoLectura(getApplication(), libroId).path
                )
            } catch (e: Exception) {
                val codigo = PdfStorage.codigoError(e)
                _uiState.value = LecturaUiState.Error(
                    if (codigo == ErrorApp.SIN_CONEXION) ErrorApp.LECTURA_SIN_PDF else codigo
                )
            }
        }
    }

    fun marcarCorrupto(libroId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            PdfStorage.archivoDescarga(getApplication(), libroId).delete()
            PdfStorage.archivoLectura(getApplication(), libroId).delete()
        }
        _uiState.value = LecturaUiState.Error(ErrorApp.PDF_DANADO)
    }
}
