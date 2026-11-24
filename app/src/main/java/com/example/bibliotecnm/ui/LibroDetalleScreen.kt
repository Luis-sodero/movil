package com.example.bibliotecnm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.viewmodel.LibroDetalleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleLibro(
    libroId: Int,
    urlPdf: String?,
    titulo: String?,
    onVolver: () -> Unit,
    onLeer: (Int, String?) -> Unit,
    viewModel: LibroDetalleViewModel = viewModel()
) {
    val descargado by viewModel.descargado.collectAsState()
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(libroId) {
        viewModel.verificar(libroId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del libro") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo ?: "Libro",
                style = MaterialTheme.typography.titleLarge
            )

            if (!urlPdf.isNullOrBlank()) {
                if (descargado) {
                    Text(
                        text = "Disponible sin conexión",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { onLeer(libroId, urlPdf) },
                    enabled = estado !is LibroDetalleViewModel.DescargaUiState.Descargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Leer libro")
                }

                if (descargado) {
                    OutlinedButton(
                        onClick = { viewModel.eliminar(libroId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Eliminar descarga")
                    }
                } else {
                    Button(
                        onClick = { viewModel.descargar(libroId, urlPdf) },
                        enabled = estado !is LibroDetalleViewModel.DescargaUiState.Descargando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Descargar para leer sin conexión")
                    }
                }

                when (val descarga = estado) {
                    is LibroDetalleViewModel.DescargaUiState.Descargando -> {
                        Text(text = "Descargando ${descarga.progreso}%")
                        LinearProgressIndicator(
                            progress = { descarga.progreso / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is LibroDetalleViewModel.DescargaUiState.Error -> {
                        Text(
                            text = descarga.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { viewModel.limpiarError() }) {
                            Text(text = "Entendido")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
