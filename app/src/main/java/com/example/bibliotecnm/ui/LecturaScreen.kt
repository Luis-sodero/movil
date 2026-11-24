package com.example.bibliotecnm.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.viewmodel.LecturaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLectura(
    libroId: Int,
    urlPdf: String?,
    onVolver: () -> Unit,
    viewModel: LecturaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(libroId, urlPdf) {
        viewModel.preparar(libroId, urlPdf)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Lectura") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val estado = uiState) {
            is LecturaViewModel.LecturaUiState.Cargando -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(text = "Abriendo PDF...")
                }
            }
            is LecturaViewModel.LecturaUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = estado.mensaje)
                    Button(onClick = { viewModel.preparar(libroId, urlPdf) }) {
                        Text(text = "Reintentar")
                    }
                }
            }
            is LecturaViewModel.LecturaUiState.Listo -> {
                VisorPdf(
                    ruta = estado.ruta,
                    onArchivoCorrupto = { viewModel.marcarCorrupto(libroId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun VisorPdf(
    ruta: String,
    onArchivoCorrupto: () -> Unit,
    modifier: Modifier = Modifier
) {
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var totalPaginas by remember { mutableStateOf(0) }
    var anchoPx by remember { mutableStateOf(0) }
    val listaState = rememberLazyListState()
    val paginaActual by remember {
        derivedStateOf { listaState.firstVisibleItemIndex + 1 }
    }
    val mutex = remember { Mutex() }

    DisposableEffect(ruta) {
        var descriptor: ParcelFileDescriptor? = null
        var creado: PdfRenderer? = null
        try {
            descriptor = ParcelFileDescriptor.open(File(ruta), ParcelFileDescriptor.MODE_READ_ONLY)
            creado = PdfRenderer(descriptor)
            renderer = creado
            totalPaginas = creado.pageCount
        } catch (e: Exception) {
            onArchivoCorrupto()
        }
        onDispose {
            try { creado?.close() } catch (e: Exception) {}
            try { descriptor?.close() } catch (e: Exception) {}
            renderer = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { anchoPx = it.width }
    ) {
        val visor = renderer
        if (visor != null && totalPaginas > 0) {
            LazyColumn(
                state = listaState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(totalPaginas) { indice ->
                    PaginaPdf(visor, indice, anchoPx, mutex)
                }
            }

            Text(
                text = "Página $paginaActual de $totalPaginas",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(Color(0xAA000000))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PaginaPdf(
    renderer: PdfRenderer,
    indice: Int,
    anchoPx: Int,
    mutex: Mutex
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope: CoroutineScope = rememberCoroutineScope()

    DisposableEffect(renderer, indice, anchoPx, mutex) {
        val job: Job = scope.launch(Dispatchers.IO) {
            if (anchoPx <= 0) return@launch
            mutex.withLock {
                var pagina: PdfRenderer.Page? = null
                try {
                    pagina = renderer.openPage(indice)
                    val escala = anchoPx.toFloat() / pagina.width.toFloat()
                    val alto = (pagina.height * escala).toInt().coerceAtLeast(1)
                    val imagen = Bitmap.createBitmap(anchoPx, alto, Bitmap.Config.ARGB_8888)
                    imagen.eraseColor(AndroidColor.WHITE)
                    pagina.render(imagen, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    withContext(Dispatchers.Main) { bitmap = imagen }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { bitmap = null }
                } finally {
                    try { pagina?.close() } catch (e: Exception) {}
                }
            }
        }

        onDispose {
            job.cancel()
            bitmap?.recycle()
            bitmap = null
        }
    }

    bitmap?.let { imagen ->
        Image(
            bitmap = imagen.asImageBitmap(),
            contentDescription = "Página ${indice + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
    }
}
