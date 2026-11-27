package com.example.bibliotecnm

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.R
import com.example.bibliotecnm.data.PrestamoDto
import com.example.bibliotecnm.data.SesionUsuario
import com.example.bibliotecnm.ui.mensaje
import com.example.bibliotecnm.viewmodel.PrestamosUiState
import com.example.bibliotecnm.viewmodel.PrestamosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    onVolver: () -> Unit = {},
    onIrADescargas: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    prestamosViewModel: PrestamosViewModel = viewModel()
) {
    val uiState by prestamosViewModel.uiState.collectAsState()
    val refrescando by prestamosViewModel.refrescando.collectAsState()
    val aviso by prestamosViewModel.aviso.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }
    var menuIdiomaExpandido by remember { mutableStateOf(false) }
    var idiomaActual by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags())
    }
    val opcionesIdioma = listOf(
        "" to stringResource(R.string.idioma_sistema),
        "es" to stringResource(R.string.idioma_es),
        "en" to stringResource(R.string.idioma_en),
        "fr" to stringResource(R.string.idioma_fr),
        "ja" to stringResource(R.string.idioma_ja)
    )
    val usuario = SesionUsuario.actual
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Recarga al volver a primer plano y cada 30s
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                prestamosViewModel.cargarPrestamos(mostrarCargando = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            prestamosViewModel.cargarPrestamos(mostrarCargando = false)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perfil_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { prestamosViewModel.cargarPrestamos(mostrarCargando = false) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.actualizar)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (usuario != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                             text = usuario.nombre?.ifBlank { null } ?: stringResource(R.string.usuario_generico),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = usuario.correo ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                R.string.rol,
                                usuario.rol?.ifBlank { null } ?: stringResource(R.string.lector_generico)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (aviso != null) {
                Text(
                    text = stringResource(aviso!!.mensaje()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when (uiState) {
                is PrestamosUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PrestamosUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = stringResource((uiState as PrestamosUiState.Error).codigo.mensaje()))
                    }
                }
                is PrestamosUiState.Exito -> {
                    val listaPrestamos = (uiState as PrestamosUiState.Exito).prestamos
                    PullToRefreshBox(
                        isRefreshing = refrescando,
                        onRefresh = { prestamosViewModel.cargarPrestamos(mostrarCargando = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(listaPrestamos) { prestamo ->
                                ItemPrestamo(prestamo = prestamo)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onIrADescargas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.mis_descargas))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { menuExpandido = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.configuracion)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = stringResource(R.string.configuracion_perfil))
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.idioma)) },
                        onClick = {
                            menuExpandido = false
                            menuIdiomaExpandido = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.cerrar_sesion)) },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        onClick = {
                            menuExpandido = false
                            onCerrarSesion()
                        }
                    )
                }

                DropdownMenu(
                    expanded = menuIdiomaExpandido,
                    onDismissRequest = { menuIdiomaExpandido = false }
                ) {
                    opcionesIdioma.forEach { (etiqueta, texto) ->
                        DropdownMenuItem(
                            text = { Text(texto) },
                            trailingIcon = {
                                if (idiomaActual == etiqueta) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            onClick = {
                                menuIdiomaExpandido = false
                                idiomaActual = etiqueta
                                AppCompatDelegate.setApplicationLocales(
                                    if (etiqueta.isEmpty()) {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(etiqueta)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemPrestamo(prestamo: PrestamoDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = prestamo.titulo?.ifBlank { null } ?: stringResource(R.string.libro_sin_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.autor,
                    prestamo.autor?.ifBlank { null } ?: stringResource(R.string.autor_desconocido)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            val fechaLimite = prestamo.fechaLimite ?: ""
            val devuelto = prestamo.fechaDevolucion != null
            Text(
                text = if (devuelto) {
                    stringResource(R.string.devuelto)
                } else {
                    stringResource(R.string.devolver_el, fechaLimite)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (devuelto) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}
