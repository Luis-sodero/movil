package com.example.bibliotecnm.data

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SyncManager(
    private val repository: PrestamoRepository,
    private val scope: CoroutineScope
) {
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch {
                val pendientes = repository.pendientes()
                pendientes.forEach { p ->
                    repository.sincronizarPrestamo(p)
                }
            }
        }
    }

    fun register(cm: ConnectivityManager) {
        cm.registerDefaultNetworkCallback(callback)
    }

    fun unregister(cm: ConnectivityManager) {
        cm.unregisterNetworkCallback(callback)
    }
}