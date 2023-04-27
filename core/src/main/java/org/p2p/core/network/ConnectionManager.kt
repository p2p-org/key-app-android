package org.p2p.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionManager(context: Context) {
    @Deprecated("Use flow instead", ReplaceWith("connectionStatus"))
    interface Listener {
        fun onConnectionChange()
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectionStatus = MutableStateFlow(true)
    val connectionStatus = _connectionStatus.asStateFlow()

    @Deprecated("Use flow instead", ReplaceWith("connectionStatus"))
    var listener: Listener? = null

    private var callback = ConnectionStatusCallback()

    init {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(),
            callback
        )

        notifyHasConnection(
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork).hasValidatedInternet()
        )
    }

    private fun notifyHasConnection(hasConnection: Boolean) {
        listener?.onConnectionChange()
        _connectionStatus.tryEmit(hasConnection)
    }

    private fun NetworkCapabilities?.hasValidatedInternet(): Boolean {
        if (this == null) return false

        return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    inner class ConnectionStatusCallback : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            notifyHasConnection(
                connectivityManager.getNetworkCapabilities(network).hasValidatedInternet()
            )
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            notifyHasConnection(networkCapabilities.hasValidatedInternet())
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            notifyHasConnection(
                connectivityManager.getNetworkCapabilities(network).hasValidatedInternet()
            )
        }
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            //already unregistered
        }
    }
}
