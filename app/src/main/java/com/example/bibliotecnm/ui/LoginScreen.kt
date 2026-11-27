package com.example.bibliotecnm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliotecnm.R
import com.example.bibliotecnm.data.RetrofitClient
import com.example.bibliotecnm.viewmodel.LoginUiState
import com.example.bibliotecnm.viewmodel.LoginViewModel

@Composable
fun PantallaLogin(
    onLoginExitoso: () -> Unit,
    onCrearCuenta: () -> Unit = {},
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val cargando = uiState is LoginUiState.Cargando

    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    val iniciarSesion = {
        loginViewModel.login(correo, password)
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Exito) {
            onLoginExitoso()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F766E), Color(0xFF042F2E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF5EEAD4), Color(0xFF14B8A6))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.logo_letra),
                    color = Color(0xFF042F2E),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_bienvenida),
                color = Color(0xFFCCFBF1),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.login_titulo),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.login_subtitulo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            loginViewModel.limpiarError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.correo_electronico)) },
                        placeholder = { Text(stringResource(R.string.correo_ejemplo)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        enabled = !cargando,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            loginViewModel.limpiarError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.contrasena)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { mostrarPassword = !mostrarPassword },
                                enabled = !cargando
                            ) {
                                Text(
                                    stringResource(
                                        if (mostrarPassword) R.string.ocultar else R.string.ver_contrasena
                                    )
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !cargando,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (mostrarPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (!cargando) iniciarSesion() }
                        )
                    )

                    val error = (uiState as? LoginUiState.Error)?.codigo
                    if (error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = stringResource(error.mensaje()),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = iniciarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !cargando,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.verificando))
                        } else {
                            Text(text = stringResource(R.string.entrar), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = onCrearCuenta,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    ) {
                        Text(stringResource(R.string.no_tienes_cuenta))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.usuario_prueba, "123456"),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.api_url, RetrofitClient.baseUrl),
                color = Color(0xFF99F6E4),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
