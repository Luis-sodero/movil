package com.example.bibliotecnm

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bibliotecnm.data.SesionUsuario
import com.example.bibliotecnm.ui.PantallaLogin

object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
    const val PERFIL = "perfil"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val inicio = if (SesionUsuario.estaActiva()) Rutas.PRINCIPAL else Rutas.LOGIN

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                onLoginExitoso = {
                    navController.navigate(Rutas.PRINCIPAL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.PRINCIPAL) {
            PantallaPrincipal(
                onIrAPerfil = { navController.navigate(Rutas.PERFIL) }
            )
        }

        composable(Rutas.PERFIL) {
            PantallaPerfil(
                onVolver = { navController.popBackStack() },
                onCerrarSesion = {
                    SesionUsuario.cerrar()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
