package com.example.bibliotecnm

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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.data.PrestamoDto
import com.example.bibliotecnm.data.SesionUsuario
import com.example.bibliotecnm.viewmodel.PrestamosUiState
import com.example.bibliotecnm.viewmodel.PrestamosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    prestamosViewModel: PrestamosViewModel = viewModel()
) {
    val uiState by prestamosViewModel.uiState.collectAsState()
    val refrescando by prestamosViewModel.refrescando.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }
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
                title = { Text("Mis Préstamos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { prestamosViewModel.cargarPrestamos(mostrarCargando = false) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
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
                            text = usuario.nombre ?: "Usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = usuario.correo ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rol: ${usuario.rol ?: "lector"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when (uiState) {
                is PrestamosUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PrestamosUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = (uiState as PrestamosUiState.Error).mensaje)
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
                        contentDescription = "Configuración"
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "Configuración de Perfil")
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cambiar información") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpandido = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        onClick = {
                            menuExpandido = false
                            onCerrarSesion()
                        }
                    )
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
            Text(text = prestamo.titulo ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Autor: ${prestamo.autor ?: ""}", style = MaterialTheme.typography.bodyMedium)
            val fechaLimite = prestamo.fechaLimite ?: ""
            val devuelto = prestamo.fechaDevolucion != null
            Text(
                text = if (devuelto) "Devuelto" else "Devolver el: $fechaLimite",
                style = MaterialTheme.typography.bodyMedium,
                color = if (devuelto) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}
