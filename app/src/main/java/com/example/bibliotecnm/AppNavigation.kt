package com.example.bibliotecnm

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bibliotecnm.R
import com.example.bibliotecnm.data.LibroVistaDto
import com.example.bibliotecnm.data.SesionUsuario
import com.example.bibliotecnm.ui.PantallaDescargas
import com.example.bibliotecnm.ui.PantallaDetalleLibro
import com.example.bibliotecnm.ui.PantallaLectura
import com.example.bibliotecnm.ui.PantallaLogin
import com.example.bibliotecnm.ui.PantallaRegistro

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val PRINCIPAL = "principal"
    const val PERFIL = "perfil"
    const val DESCARGAS = "descargas"
    const val DETALLE = "detalle/{libroId}?urlPdf={urlPdf}&titulo={titulo}"
    const val LECTURA = "lectura/{libroId}?urlPdf={urlPdf}"

    fun detalle(libro: LibroVistaDto, tituloGenerico: String): String {
        val id = libro.id ?: 0
        val url = Uri.encode(libro.urlPdf ?: "")
        val titulo = Uri.encode(libro.titulo ?: tituloGenerico)
        return "detalle/$id?urlPdf=$url&titulo=$titulo"
    }

    fun lectura(libroId: Int, urlPdf: String?): String =
        "lectura/$libroId?urlPdf=${Uri.encode(urlPdf ?: "")}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tituloGenerico = stringResource(R.string.libro_generico)
    val inicio = if (SesionUsuario.estaActiva()) Rutas.PRINCIPAL else Rutas.LOGIN

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                onLoginExitoso = {
                    navController.navigate(Rutas.PRINCIPAL) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onCrearCuenta = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            PantallaRegistro(
                onRegistroExitoso = {
                    navController.navigate(Rutas.PRINCIPAL) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onVolverAlLogin = { navController.popBackStack() }
            )
        }

        composable(Rutas.PRINCIPAL) {
            PantallaPrincipal(
                onIrAPerfil = { navController.navigate(Rutas.PERFIL) },
                onIrADetalle = { libro -> navController.navigate(Rutas.detalle(libro, tituloGenerico)) }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(
                navArgument("libroId") { type = NavType.IntType },
                navArgument("urlPdf") { type = NavType.StringType; defaultValue = "" },
                navArgument("titulo") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            PantallaDetalleLibro(
                libroId = entry.arguments?.getInt("libroId") ?: 0,
                urlPdf = entry.arguments?.getString("urlPdf")?.ifBlank { null },
                titulo = entry.arguments?.getString("titulo"),
                onVolver = { navController.popBackStack() },
                onLeer = { id, url -> navController.navigate(Rutas.lectura(id, url)) }
            )
        }

        composable(
            route = Rutas.LECTURA,
            arguments = listOf(
                navArgument("libroId") { type = NavType.IntType },
                navArgument("urlPdf") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            PantallaLectura(
                libroId = entry.arguments?.getInt("libroId") ?: 0,
                urlPdf = entry.arguments?.getString("urlPdf")?.ifBlank { null },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.DESCARGAS) {
            PantallaDescargas(
                onVolver = { navController.popBackStack() },
                onLeer = { id -> navController.navigate(Rutas.lectura(id, null)) }
            )
        }

        composable(Rutas.PERFIL) {
            PantallaPerfil(
                onVolver = { navController.popBackStack() },
                onIrADescargas = { navController.navigate(Rutas.DESCARGAS) },
                onCerrarSesion = {
                    SesionUsuario.cerrar(context)
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
