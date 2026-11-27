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

class LibroDetalleViewModel(application: Application) : AndroidViewModel(application) {
    sealed class DescargaUiState {
        object Inactivo : DescargaUiState()
        data class Descargando(val progreso: Int) : DescargaUiState()
        object Listo : DescargaUiState()
        data class Error(val codigo: ErrorApp) : DescargaUiState()
    }

    private val _descargado = MutableStateFlow(false)
    val descargado: StateFlow<Boolean> = _descargado.asStateFlow()

    private val _estado = MutableStateFlow<DescargaUiState>(DescargaUiState.Inactivo)
    val estado: StateFlow<DescargaUiState> = _estado.asStateFlow()

    fun verificar(libroId: Int) {
        _descargado.value = PdfStorage.archivoDescarga(getApplication(), libroId).exists()
    }

    fun descargar(libroId: Int, urlPdf: String?) {
        val url = RetrofitClient.urlPdf(urlPdf)
        if (url.isNullOrBlank()) {
            _estado.value = DescargaUiState.Error(ErrorApp.PDF_NO_DISPONIBLE)
            return
        }

        viewModelScope.launch {
            _estado.value = DescargaUiState.Descargando(0)
            try {
                PdfStorage.descargar(
                    url = url,
                    destino = PdfStorage.archivoDescarga(getApplication(), libroId),
                    temporal = PdfStorage.archivoTemporalDescarga(getApplication(), libroId),
                    onProgreso = { progreso ->
                        _estado.value = DescargaUiState.Descargando(progreso)
                    }
                )
                _descargado.value = true
                _estado.value = DescargaUiState.Listo
            } catch (e: Exception) {
                _estado.value = DescargaUiState.Error(PdfStorage.codigoError(e))
            }
        }
    }

    fun eliminar(libroId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            PdfStorage.archivoDescarga(getApplication(), libroId).delete()
            PdfStorage.archivoTemporalDescarga(getApplication(), libroId).delete()
        }
        _descargado.value = false
        _estado.value = DescargaUiState.Inactivo
    }

    fun limpiarError() {
        _estado.value = DescargaUiState.Inactivo
    }
}
