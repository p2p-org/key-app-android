package org.p2p.core.network.environment

import android.content.Context
import org.p2p.core.R
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.network.NetworkServicesUrlStorage

private const val KEY_NOTIFICATION_SERVICE_BASE_URL = "KEY_NOTIFICATION_SERVICE_BASE_URL"
private const val KEY_FEE_RELAYER_BASE_URL = "KEY_FEE_RELAYER_BASE_URL"
private const val KEY_NAME_SERVICE_BASE_URL = "KEY_NAME_SERVICE_BASE_URL"

private const val KEY_BRIDGES_SERVICE_BASE_URL = "KEY_BRIDGES_SERVICE_BASE_URL"
private const val KEY_GATEWAY_BASE_URL = "KEY_GATEWAY_BASE_URL"
private const val KEY_TOKEN_SERVICE_BASE_URL = "KEY_TOKEN_SERVICE_BASE_URL"

class NetworkServicesUrlProvider(
    private val context: Context,
    private val storage: NetworkServicesUrlStorage,
    private val crashLogger: CrashLogger
) {

    private val moonpayEnvironmentProvider = MoonpayEnvironmentProvider(
        context = context,
        storage = storage,
        crashLogger = crashLogger
    )

    private val torusEnvironmentProvider = TorusEnvironmentProvider(
        context = context,
        storage = storage
    )

    fun loadFeeRelayerEnvironment(): FeeRelayerEnvironment {
        val url = storage.getString(
            KEY_FEE_RELAYER_BASE_URL,
            context.getString(R.string.feeRelayerBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_FEE_RELAYER_BASE_URL, url)
        return FeeRelayerEnvironment(url)
    }

    fun toggleFeeRelayerEnvironment(isProdSelected: Boolean) {
        val newUrl = if (isProdSelected) R.string.feeRelayerBaseUrl else R.string.feeRelayerTestBaseUrl
        storage.putString(KEY_FEE_RELAYER_BASE_URL, context.getString(newUrl))
    }

    fun loadNameServiceEnvironment(): NameServiceEnvironment {
        val url = storage.getString(
            KEY_NAME_SERVICE_BASE_URL,
            context.getString(R.string.registerUsernameServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_NAME_SERVICE_BASE_URL, url)
        return NameServiceEnvironment(
            baseUrl = url,
            isProductionSelected = isProdUrl(url, R.string.registerUsernameServiceBaseUrl)
        )
    }

    fun toggleNameServiceEnvironment(isProdSelected: Boolean) {
        val newUrl = if (isProdSelected) {
            R.string.registerUsernameServiceBaseUrl
        } else {
            R.string.registerUsernameServiceTestUrl
        }
        storage.putString(KEY_NAME_SERVICE_BASE_URL, context.getString(newUrl))
    }

    fun loadNotificationServiceEnvironment(): NotificationServiceEnvironment {
        val url = storage.getString(
            KEY_NOTIFICATION_SERVICE_BASE_URL,
            context.getString(R.string.notificationServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_NOTIFICATION_SERVICE_BASE_URL, url)
        return NotificationServiceEnvironment(
            baseUrl = url,
            isProdSelected = isProdUrl(url, R.string.notificationServiceBaseUrl)
        )
    }

    fun toggleNotificationServiceEnvironment(isProdSelected: Boolean) {
        val newUrl = if (isProdSelected) {
            R.string.notificationServiceBaseUrl
        } else {
            R.string.notificationServiceTestBaseUrl
        }
        storage.putString(KEY_NOTIFICATION_SERVICE_BASE_URL, context.getString(newUrl))
    }

    fun loadBridgesServiceEnvironment(): BridgesServiceEnvironment {
        val url = storage.getString(
            KEY_BRIDGES_SERVICE_BASE_URL,
            context.getString(R.string.bridgesServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_BRIDGES_SERVICE_BASE_URL, url)

        return BridgesServiceEnvironment(url)
    }

    fun saveBridgesServiceEnvironment(newUrl: String) {
        storage.putString(KEY_BRIDGES_SERVICE_BASE_URL, newUrl)
    }

    fun loadGatewayServiceEnvironment(): GatewayServiceEnvironment {
        val url = storage.getString(
            KEY_GATEWAY_BASE_URL,
            context.getString(R.string.web3AuthServiceBaseUrl)
        ).orEmpty()

        return GatewayServiceEnvironment(
            baseUrl = url,
            isProdSelected = isProdUrl(url, R.string.web3AuthServiceBaseUrl)
        )
    }

    fun loadTokenServiceEnvironment(): TokenServiceEnvironment {
        val url = storage.getString(
            KEY_TOKEN_SERVICE_BASE_URL,
            context.getString(R.string.tokenServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_TOKEN_SERVICE_BASE_URL, url)
        return TokenServiceEnvironment(
            baseServiceUrl = url,
            isProdSelected = isProdUrl(url, R.string.tokenServiceBaseUrl)
        )
    }

    fun toggleTokenServiceEnvironment(isProdSelected: Boolean) {
        val newUrl = if (isProdSelected) R.string.tokenServiceBaseUrl else R.string.tokenServiceTestBaseUrl
        storage.putString(KEY_TOKEN_SERVICE_BASE_URL, context.getString(newUrl))
    }

    fun loadTorusEnvironment(): TorusEnvironment =
        torusEnvironmentProvider.loadTorusEnvironment()

    fun saveTorusEnvironment(newUrl: String?, newVerifier: String?, newSubVerifier: String?) =
        torusEnvironmentProvider.saveTorusEnvironment(newUrl, newVerifier, newSubVerifier)

    fun loadMoonpayEnvironment(): MoonpayEnvironment =
        moonpayEnvironmentProvider.loadMoonpayEnvironment()

    fun toggleMoonpayEnvironment(isSandboxSelected: Boolean) =
        moonpayEnvironmentProvider.toggleMoonpayEnvironment(isSandboxSelected)

    fun resetMoonpayEnvironment() =
        moonpayEnvironmentProvider.resetMoonpayEnvironment()

    private fun isProdUrl(currentUrl: String, prodUrlRes: Int): Boolean {
        return currentUrl == context.getString(prodUrlRes)
    }
}
