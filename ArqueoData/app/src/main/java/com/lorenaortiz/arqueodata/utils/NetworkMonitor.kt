package com.lorenaortiz.arqueodata.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * Clase que monitorea el estado de la conexión a internet.
 * Proporcionamos un flujo de datos que emite el estado actual de la conexión.
 */
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    fun isOnline(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Definimos el callback para manejar los cambios en la red
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            // Notificamos cuando una red está disponible
            override fun onAvailable(network: Network) {
                networks.add(network)
                trySend(true)
            }

            // Notificamos cuando se pierde una red
            override fun onLost(network: Network) {
                networks.remove(network)
                trySend(networks.isNotEmpty())
            }
        }

        // Configuramos la solicitud de red para monitorear la conectividad a internet
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Enviamos el estado inicial de la conexión
        val currentState = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        trySend(currentState)

        // Limpiamos el callback cuando el flujo se cierra
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
} 