package com.example.bibliotecnm.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException

object PdfStorage {
    private const val CARPETA = "pdfs"

    fun archivoDescarga(context: Context, libroId: Int): File =
        File(File(context.filesDir, CARPETA), "$libroId.pdf")

    fun archivoTemporalDescarga(context: Context, libroId: Int): File =
        File(File(context.filesDir, CARPETA), "$libroId.pdf.tmp")

    fun archivoLectura(context: Context, libroId: Int): File =
        File(File(context.cacheDir, CARPETA), "$libroId.pdf")

    fun archivoTemporalLectura(context: Context, libroId: Int): File =
        File(File(context.cacheDir, CARPETA), "$libroId.pdf.tmp")

    suspend fun descargar(
        url: String,
        destino: File,
        temporal: File,
        onProgreso: (Int) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                destino.parentFile?.mkdirs()
                val respuesta = RetrofitClient.apiService.descargarPdf(url)
                val total = respuesta.contentLength()
                var leidos = 0L
                val buffer = ByteArray(8 * 1024)
                respuesta.byteStream().use { entrada ->
                    temporal.outputStream().use { salida ->
                        while (true) {
                            val cantidad = entrada.read(buffer)
                            if (cantidad <= 0) break
                            salida.write(buffer, 0, cantidad)
                            leidos += cantidad
                            if (total > 0) {
                                onProgreso(((leidos * 100) / total).toInt().coerceIn(0, 99))
                            }
                        }
                    }
                }
                onProgreso(100)
                if (!temporal.renameTo(destino)) throw IOException("No se pudo guardar el PDF")
            } catch (e: Exception) {
                temporal.delete()
                throw e
            }
        }
    }

    fun mensajeError(error: Throwable): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                404 -> "No se encontró el PDF en el servidor."
                else -> "Error del servidor (${error.code()}). Intenta de nuevo."
            }
            is IOException -> {
                val mensaje = error.message.orEmpty()
                if (mensaje.contains("No space left", ignoreCase = true)) {
                    "Sin espacio en el dispositivo. Libera espacio e intenta de nuevo."
                } else {
                    "Sin conexión. Revisa tu internet e intenta de nuevo."
                }
            }
            else -> "No se pudo abrir el PDF. Intenta de nuevo."
        }
    }
}
