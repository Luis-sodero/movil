package com.example.bibliotecnm.data

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SyncManager(
    private val repository: PrestamoRepository,
    private val scope: CoroutineScope,
    private val onSynced: suspend () -> Unit = {}
) {
    private var registered = false
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch {
                sincronizarPendientes()
            }
        }
    }

    suspend fun sincronizarPendientes() {
        val pendientes = repository.pendientes()
        var huboSincronizados = false
        pendientes.forEach { p ->
            val res = repository.sincronizarPrestamo(p)
            if (res.isSuccess) {
                huboSincronizados = true
            }
        }
        if (huboSincronizados) {
            onSynced()
        }
    }

    fun register(cm: ConnectivityManager) {
        if (registered) return
        try {
            cm.registerDefaultNetworkCallback(callback)
            registered = true
        } catch (_: Exception) {
        }
    }

    fun unregister(cm: ConnectivityManager) {
        if (!registered) return
        try {
            cm.unregisterNetworkCallback(callback)
            registered = false
        } catch (_: Exception) {
        }
    }
}