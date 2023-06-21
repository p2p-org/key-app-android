package org.p2p.core.network.environment

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.crashlytics.CrashLogger
import timber.log.Timber
import kotlin.reflect.KClass

private const val KEY_BASE_URL = "KEY_BASE_URL"
private const val KEY_RPC_BASE_URL = "KEY_RPC_BASE_URL"

class NetworkEnvironmentManager(
    private val sharedPreferences: SharedPreferences,
    private val crashLogger: CrashLogger,
    private val networksFromRemoteConfig: List<NetworkEnvironment>
) {

    fun interface EnvironmentManagerListener {
        fun onEnvironmentChanged(newEnvironment: NetworkEnvironment)
    }

    val availableNetworks: List<NetworkEnvironment>
        get() = networksFromRemoteConfig

    private var listeners = mutableMapOf<String, EnvironmentManagerListener>()

    fun addEnvironmentListener(owner: KClass<*>, listener: EnvironmentManagerListener) {
        listeners[owner.simpleName.orEmpty()] = listener
    }

    fun removeEnvironmentListener(owner: KClass<*>) {
        listeners.remove(owner.simpleName.orEmpty())
    }

    fun isDevnet(): Boolean = loadCurrentEnvironment() == NetworkEnvironment.DEVNET

    fun isMainnet(): Boolean =
        loadCurrentEnvironment() in listOf(
            NetworkEnvironment.MAINNET,
            NetworkEnvironment.RPC_POOL,
            NetworkEnvironment.SOLANA
        )

    fun loadCurrentEnvironment(): NetworkEnvironment {
        return sharedPreferences.getString(KEY_BASE_URL, null)
            ?.let(::getCurrentNetworkFromUrl)
            ?.takeIf(availableNetworks::contains)
            ?: getDefaultAvailableNetwork()
    }

    private fun getDefaultAvailableNetwork(): NetworkEnvironment {
        return if (NetworkEnvironment.RPC_POOL in availableNetworks) {
            NetworkEnvironment.RPC_POOL
        } else {
            availableNetworks.first()
        }
    }

    fun loadRpcEnvironment(): RpcEnvironment = if (loadCurrentEnvironment() == NetworkEnvironment.DEVNET) {
        RpcEnvironment.DEVNET
    } else {
        RpcEnvironment.MAINNET
    }

    fun chooseEnvironment(newEnvironment: NetworkEnvironment) {
        val newEndpoint = newEnvironment.endpoint
        val newRpcEnvironment = getRpcEnvironmentFromUrl(newEndpoint)

        sharedPreferences.edit(commit = true) {
            putString(KEY_BASE_URL, newEndpoint)
            putString(KEY_RPC_BASE_URL, newRpcEnvironment.endpoint)
        }

        listeners.values.forEach { it.onEnvironmentChanged(newEnvironment) }

        crashLogger.setCustomKey("network_environment", newEnvironment.endpoint)

        Timber.i("Network changed to ${newEnvironment.endpoint}")
    }

    private fun getCurrentNetworkFromUrl(endpoint: String): NetworkEnvironment = when (endpoint) {
        NetworkEnvironment.MAINNET.endpoint -> NetworkEnvironment.MAINNET
        NetworkEnvironment.DEVNET.endpoint -> NetworkEnvironment.DEVNET
        NetworkEnvironment.SOLANA.endpoint -> NetworkEnvironment.SOLANA
        NetworkEnvironment.RPC_POOL.endpoint -> NetworkEnvironment.RPC_POOL
        else -> {
            Timber.e(IllegalArgumentException("Unknown endpoint $endpoint, switching..."))
            chooseEnvironment(getDefaultAvailableNetwork())
            getDefaultAvailableNetwork()
        }
    }

    private fun getRpcEnvironmentFromUrl(endpoint: String): RpcEnvironment {
        return if (NetworkEnvironment.DEVNET.endpoint == endpoint) RpcEnvironment.DEVNET else RpcEnvironment.MAINNET
    }
}
