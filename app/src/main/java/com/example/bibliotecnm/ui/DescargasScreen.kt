package com.example.bibliotecnm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.R
import com.example.bibliotecnm.data.RetrofitClient
import com.example.bibliotecnm.viewmodel.DescargaItem
import com.example.bibliotecnm.viewmodel.DescargasViewModel
import coil.compose.AsyncImage
import android.text.format.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDescargas(
    onVolver: () -> Unit = {},
    onLeer: (Int) -> Unit = {},
    viewModel: DescargasViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val context = LocalContext.current
    var itemConfirmado by remember { mutableStateOf<DescargaItem?>(null) }

    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.mis_descargas)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
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
            if (cargando) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.descargas_vacias),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = RetrofitClient.urlImagen(item.urlPortada),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(width = 56.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.ic_book_placeholder),
                                    error = painterResource(R.drawable.ic_book_placeholder)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.titulo ?: stringResource(R.string.libro_sin_titulo),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    if (item.autor != null) {
                                        Text(
                                            text = stringResource(R.string.autor, item.autor),
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2
                                        )
                                    }
                                    Text(
                                        text = Formatter.formatFileSize(context, item.tamano),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Button(onClick = { onLeer(item.libroId) }) {
                                            Text(text = stringResource(R.string.leer))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(onClick = { itemConfirmado = item }) {
                                            Text(text = stringResource(R.string.eliminar_descarga))
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

    val confirmado = itemConfirmado
    if (confirmado != null) {
        AlertDialog(
            onDismissRequest = { itemConfirmado = null },
            title = { Text(text = stringResource(R.string.eliminar_descarga)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.eliminar_descarga_pregunta,
                        confirmado.titulo ?: stringResource(R.string.libro_sin_titulo)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(confirmado.libroId)
                    itemConfirmado = null
                }) {
                    Text(text = stringResource(R.string.eliminar))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemConfirmado = null }) {
                    Text(text = stringResource(R.string.cancelar))
                }
            }
        )
    }
}
