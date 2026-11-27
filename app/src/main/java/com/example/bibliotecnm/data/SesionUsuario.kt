package com.example.bibliotecnm.data

import android.content.Context

object SesionUsuario {
    private const val PREFS = "bibliotecnm_sesion"
    private const val KEY_ID = "id"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_CORREO = "correo"
    private const val KEY_ROL = "rol"

    @Volatile
    private var usuario: LoginResponse? = null

    val actual: LoginResponse?
        get() = usuario

    val id: Int
        get() = usuario?.id ?: 0

    fun iniciar(context: Context, usuario: LoginResponse) {
        this.usuario = usuario
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_ID, usuario.id ?: 0)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putString(KEY_CORREO, usuario.correo)
            .putString(KEY_ROL, usuario.rol)
            .apply()
    }

    fun restaurar(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_ID, 0)
        usuario = if (id > 0) {
            LoginResponse(
                id = id,
                nombre = prefs.getString(KEY_NOMBRE, null),
                correo = prefs.getString(KEY_CORREO, null),
                rol = prefs.getString(KEY_ROL, null)
            )
        } else {
            null
        }
    }

    fun cerrar(context: Context) {
        usuario = null
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun estaActiva(): Boolean = id > 0
}
