package org.p2p.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import timber.log.Timber
import java.io.IOException
import java.net.InetAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val INET_VALIDATION_MAX_RETRIES = 3
private const val INET_VALIDATION_INTERVAL = 1000L

class ConnectionManager(
    context: Context,
    private val scope: CoroutineScope,
    private val checkInetDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val checkInetHost: String = "8.8.8.8",
    private val checkInetTimeoutMs: Int = 5000
) {
    @Deprecated("Use flow instead", ReplaceWith("connectionStatus"))
    interface Listener {
        fun onConnectionChange()
    }

    @Deprecated("Use flow instead", ReplaceWith("connectionStatus"))
    var listener: Listener? = null

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var callback = ConnectionStatusCallback()
    private var checkNetworkJob: Job? = null

    private val _connectionStatus = MutableStateFlow(true)
    val connectionStatus = _connectionStatus.asStateFlow()

    init {
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        notifyHasConnection()
    }

    fun stop() {
        Timber.d("Stopping connection manager")
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // already unregistered
        }
    }

    /**
     * This function instantly checks the internet and returns the result.
     * Attention: this function DOES NOT notify [connectionStatus] and its subscribers.
     */
    suspend fun checkNow(): Boolean = withContext(checkInetDispatcher) {
        checkAnyTransportIsAvailable() && checkRealConnection()
    }

    /**
     * Check if there exists any network transport.
     */
    private fun checkAnyTransportIsAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    private fun notifyHasConnection() {
        listener?.onConnectionChange()

        checkNetworkJob?.cancel()
        checkNetworkJob = scope.launch {
            try {
                var retries = 0
                var hasVerified: Boolean
                do {
                    hasVerified = checkNow()

                    if (hasVerified) break

                    delay(INET_VALIDATION_INTERVAL)
                } while (++retries < INET_VALIDATION_MAX_RETRIES)

                // we should delay, connecting after this validation is not happening immediately
                // keep this magic number, until we don't have a better solution
                delay(1000)
                _connectionStatus.emit(hasVerified)
            } catch (_: CancellationException) {
                // ignore
            }
        }
    }

    /**
     * Check if there is a real connection to the internet (checking google dns).
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun checkRealConnection(): Boolean = withContext(checkInetDispatcher) {
        try {
            val inetAddress = InetAddress.getByName(checkInetHost)
            inetAddress.isReachable(checkInetTimeoutMs)
        } catch (e: IOException) {
            false
        }
    }

    inner class ConnectionStatusCallback : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            notifyHasConnection()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            notifyHasConnection()
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            notifyHasConnection()
        }
    }
}
