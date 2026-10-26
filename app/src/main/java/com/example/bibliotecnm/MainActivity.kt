package com.example.bibliotecnm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.data.LibroVistaDto
import com.example.bibliotecnm.ui.theme.BibliotecnmTheme
import com.example.bibliotecnm.viewmodel.LibrosUiState
import com.example.bibliotecnm.viewmodel.LibrosViewModel
import com.example.bibliotecnm.viewmodel.SolicitudUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibliotecnmTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    onIrAPerfil: () -> Unit = {},
    librosViewModel: LibrosViewModel = viewModel()
) {
    var textSearch by remember { mutableStateOf("") }
    var activeSearch by remember { mutableStateOf(false) }
    var libroSeleccionado by remember { mutableStateOf<LibroVistaDto?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val uiState by librosViewModel.uiState.collectAsState()
    val solicitudState by librosViewModel.solicitudState.collectAsState()
    val context = LocalContext.current

    // Muestra el resultado de la solicitud una sola vez
    LaunchedEffect(solicitudState) {
        when (val estado = solicitudState) {
            is SolicitudUiState.Exito -> {
                Toast.makeText(context, estado.mensaje, Toast.LENGTH_SHORT).show()
                librosViewModel.resetSolicitudState()
            }
            is SolicitudUiState.Error -> {
                Toast.makeText(context, estado.mensaje, Toast.LENGTH_SHORT).show()
                librosViewModel.resetSolicitudState()
            }
            else -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    modifier = Modifier.weight(1f),
                    query = textSearch,
                    onQueryChange = { textSearch = it },
                    onSearch = { activeSearch = false },
                    active = activeSearch,
                    onActiveChange = { activeSearch = it },
                    placeholder = { Text("Buscar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (activeSearch) {
                            IconButton(
                                onClick = {
                                    if (textSearch.isNotEmpty()) textSearch = "" else activeSearch = false
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                ) {
                    Text(text = "Sugerencias...", modifier = Modifier.padding(16.dp))
                }

                if (!activeSearch) {
                    IconButton(
                        onClick = onIrAPerfil,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Ir al perfil")
                    }
                }
            }

            if (!activeSearch) {
                Text(
                    text = "Títulos Recomendados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                when (val estado = uiState) {
                    is LibrosUiState.Cargando -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is LibrosUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = estado.mensaje)
                        }
                    }

                    is LibrosUiState.Exito -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(estado.libros) { libro ->
                                val disponibles = libro.ejemplaresDisponibles ?: 0

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 60.dp, height = 90.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "Libro", color = Color.Gray)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = libro.titulo ?: "",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Autor: ${libro.autores ?: "-"}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Géneros: ${libro.generos ?: "-"}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Año: ${libro.anio ?: "-"}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = if (disponibles > 0) "Disponible ($disponibles)" else "No disponible",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (disponibles > 0) Color(0xFF2E7D32) else Color.Red
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                libroSeleccionado = libro
                                                mostrarDialogo = true
                                            },
                                            enabled = disponibles > 0 &&
                                                    solicitudState !is SolicitudUiState.Cargando
                                        ) {
                                            Text(text = "Solicitar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val libro = libroSeleccionado
    if (mostrarDialogo && libro != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(text = "Confirmar préstamo") },
            text = { Text(text = "¿Solicitar préstamo de \"${libro.titulo ?: ""}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        libro.id?.let { librosViewModel.solicitarPrestamo(it) }
                        mostrarDialogo = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}