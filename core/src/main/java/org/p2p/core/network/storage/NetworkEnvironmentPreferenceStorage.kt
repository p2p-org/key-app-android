package org.p2p.core.network.storage

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.RpcEnvironment

private const val KEY_BASE_URL = "KEY_BASE_URL"
private const val KEY_RPC_BASE_URL = "KEY_RPC_BASE_URL"

class NetworkEnvironmentPreferenceStorage(
    private val preferences: SharedPreferences
) : NetworkEnvironmentStorage {

    override fun getCurrentEnvironment(): NetworkEnvironment? {
        return preferences.getString(KEY_BASE_URL, null)
            ?.let(::getCurrentNetworkFromUrl)
    }

    override fun updateCurrentEnvironment(newEnvironment: NetworkEnvironment) {
        val newEndpoint = newEnvironment.endpoint
        val newRpcEnvironment = getRpcEnvironmentFromUrl(newEndpoint)

        preferences.edit(commit = true) {
            putString(KEY_BASE_URL, newEndpoint)
            putString(KEY_RPC_BASE_URL, newRpcEnvironment.endpoint)
        }
    }

    private fun getCurrentNetworkFromUrl(endpoint: String): NetworkEnvironment? = when (endpoint) {
        NetworkEnvironment.MAINNET.endpoint -> NetworkEnvironment.MAINNET
        NetworkEnvironment.DEVNET.endpoint -> NetworkEnvironment.DEVNET
        NetworkEnvironment.SOLANA.endpoint -> NetworkEnvironment.SOLANA
        NetworkEnvironment.RPC_POOL.endpoint -> NetworkEnvironment.RPC_POOL
        "https://p2p.rpcpool.com" -> NetworkEnvironment.RPC_POOL
        else -> null
    }

    private fun getRpcEnvironmentFromUrl(endpoint: String): RpcEnvironment {
        return if (NetworkEnvironment.DEVNET.endpoint == endpoint) RpcEnvironment.DEVNET else RpcEnvironment.MAINNET
    }
}
