package org.p2p.wallet.infrastructure.network.environment

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.solanaj.rpc.RpcEnvironment
import org.p2p.wallet.common.feature_toggles.toggles.remote.SettingsNetworkListFeatureToggle
import kotlin.reflect.KClass

private const val KEY_BASE_URL = "KEY_BASE_URL"
private const val KEY_RPC_BASE_URL = "KEY_RPC_BASE_URL"

class NetworkEnvironmentManager(
    private val sharedPreferences: SharedPreferences,
    private val networkListFeatureToggle: SettingsNetworkListFeatureToggle
) {

    fun interface EnvironmentManagerListener {
        fun onEnvironmentChanged(newEnvironment: NetworkEnvironment)
    }

    val availableNetworks: List<NetworkEnvironment>
        get() = loadAvailableEnvironments()

    private var listeners = mutableMapOf<String, EnvironmentManagerListener>()

    private fun loadAvailableEnvironments(): List<NetworkEnvironment> {
        val networksFromRemoteConfig = networkListFeatureToggle.value.map { it.url }
        val isNetworkAvailable = { network: NetworkEnvironment -> network.endpoint in networksFromRemoteConfig }
        //Temporary remove for test
        return NetworkEnvironment.values().toList()
    }

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

        sharedPreferences.edit {
            putString(KEY_BASE_URL, newEndpoint)
            putString(KEY_RPC_BASE_URL, newRpcEnvironment.endpoint)
        }

        listeners.values.forEach { it.onEnvironmentChanged(newEnvironment) }
    }

    private fun getCurrentNetworkFromUrl(endpoint: String): NetworkEnvironment = when (endpoint) {
        NetworkEnvironment.MAINNET.endpoint -> NetworkEnvironment.MAINNET
        NetworkEnvironment.DEVNET.endpoint -> NetworkEnvironment.DEVNET
        NetworkEnvironment.SOLANA.endpoint -> NetworkEnvironment.SOLANA
        NetworkEnvironment.RPC_POOL.endpoint -> NetworkEnvironment.RPC_POOL
        else -> error("Unknown endpoint $endpoint")
    }

    private fun getRpcEnvironmentFromUrl(endpoint: String): RpcEnvironment {
        return if (NetworkEnvironment.DEVNET.endpoint == endpoint) RpcEnvironment.DEVNET else RpcEnvironment.MAINNET
    }
}
