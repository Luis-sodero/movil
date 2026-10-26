package com.example.bibliotecnm.data

object SesionUsuario {
    @Volatile
    private var usuario: LoginResponse? = null

    val actual: LoginResponse?
        get() = usuario

    val id: Int
        get() = usuario?.id ?: 0

    fun iniciar(usuario: LoginResponse) {
        this.usuario = usuario
    }

    fun cerrar() {
        usuario = null
    }

    fun estaActiva(): Boolean = id > 0
}
