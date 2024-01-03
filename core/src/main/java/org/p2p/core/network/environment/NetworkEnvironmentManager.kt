package org.p2p.core.network.environment

import timber.log.Timber
import kotlin.reflect.KClass
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.network.storage.NetworkEnvironmentStorage

class NetworkEnvironmentManager(
    private val networkEnvironmentStorage: NetworkEnvironmentStorage,
    private val crashLogger: CrashLogger,
    private val networksFromRemoteConfig: List<NetworkEnvironment>
) {

    companion object {
        const val URL_PRIVACY_POLICY = "https://key.app/privacypolicy"
        const val URL_TERMS_OF_USE = "https://key.app/termsofservice"
    }

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
        return networkEnvironmentStorage.getCurrentEnvironment()
            ?.takeIf(availableNetworks::contains)
            ?: getDefaultAvailableNetwork()
    }

    private fun getDefaultAvailableNetwork(): NetworkEnvironment {
        return NetworkEnvironment.RPC_POOL
    }

    fun loadRpcEnvironment(): RpcEnvironment = if (loadCurrentEnvironment() == NetworkEnvironment.DEVNET) {
        RpcEnvironment.DEVNET
    } else {
        RpcEnvironment.MAINNET
    }

    fun chooseEnvironment(newEnvironment: NetworkEnvironment) {
        networkEnvironmentStorage.updateCurrentEnvironment(newEnvironment)

        listeners.values.forEach { it.onEnvironmentChanged(newEnvironment) }
        crashLogger.setCustomKey("network_environment", newEnvironment.endpoint)
        Timber.i("Network changed to ${newEnvironment.endpoint}")
    }
}
