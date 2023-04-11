package org.p2p.wallet.updates

import androidx.core.content.getSystemService
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class NetworkConnectionStateProvider(context: Context) {
    private val connectivityManager: ConnectivityManager = context.getSystemService()!!
    private val networkState = MutableStateFlow(hasConnection())
    private var subscribersCount = 0

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkState.value = hasConnection()
        }

        override fun onLost(network: Network) {
            networkState.value = hasConnection()
        }
    }

    fun getNetworkStateFlow(): Flow<Boolean> = networkState
        .onStart { registerNetworkCallback() }
        .onCompletion { unregisterNetworkCallback() }

    @Synchronized
    private fun registerNetworkCallback() {
        if (subscribersCount++ == 0) {
            try {
                connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
            } catch (e: IllegalArgumentException) {
                // to prevent java.lang.IllegalArgumentException: Too many NetworkRequests filed
            }
        }
    }

    @Synchronized
    private fun unregisterNetworkCallback() {
        if (--subscribersCount == 0) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: IllegalArgumentException) {
                // to prevent java.lang.IllegalArgumentException: NetworkCallback was already unregistered
            }
        }
    }

    suspend fun awaitNetwork() {
        networkState.first { it }
    }

    // PWN-3970
    @Suppress("DEPRECATION")
    fun hasConnection(): Boolean = connectivityManager.allNetworks.any { network ->
        connectivityManager.getNetworkCapabilities(network)
            ?.run {
                hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            }
            ?: false
    }
}
