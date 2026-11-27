package com.example.bibliotecnm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecnm.data.DataBaseLocal
import com.example.bibliotecnm.data.PdfStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DescargaItem(
    val libroId: Int,
    val titulo: String?,
    val autor: String?,
    val urlPortada: String?,
    val tamano: Long
)

class DescargasViewModel(application: Application) : AndroidViewModel(application) {
    private val libroDao = DataBaseLocal.getDatabase(application).libroDao()

    private val _items = MutableStateFlow<List<DescargaItem>>(emptyList())
    val items: StateFlow<List<DescargaItem>> = _items.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            val descargas = withContext(Dispatchers.IO) {
                PdfStorage.listarDescargas(getApplication()).map { (id, archivo) ->
                    val libro = libroDao.buscar(id)
                    DescargaItem(
                        libroId = id,
                        titulo = libro?.titulo?.takeIf { it.isNotBlank() },
                        autor = libro?.autores?.takeIf { it.isNotBlank() },
                        urlPortada = libro?.urlPortada?.takeIf { it.isNotBlank() },
                        tamano = archivo.length()
                    )
                }.sortedBy { it.titulo.orEmpty() }
            }
            _items.value = descargas
            _cargando.value = false
        }
    }

    fun eliminar(libroId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            PdfStorage.archivoDescarga(getApplication(), libroId).delete()
            PdfStorage.archivoTemporalDescarga(getApplication(), libroId).delete()
        }
        cargar()
    }
}
