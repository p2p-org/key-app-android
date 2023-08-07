package org.p2p.core.network.environment

import android.content.Context
import timber.log.Timber
import org.p2p.core.BuildConfig
import org.p2p.core.R
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.network.NetworkServicesUrlStorage
import org.p2p.core.utils.getStringResourceByName

private const val KEY_NOTIFICATION_SERVICE_BASE_URL = "KEY_NOTIFICATION_SERVICE_BASE_URL"
private const val KEY_FEE_RELAYER_BASE_URL = "KEY_FEE_RELAYER_BASE_URL"
private const val KEY_NAME_SERVICE_BASE_URL = "KEY_NAME_SERVICE_BASE_URL"
private const val KEY_TORUS_BASE_URL = "KEY_TORUS_BASE_URL"
private const val KEY_TORUS_BASE_VERIFIER = "KEY_TORUS_BASE_VERIFIER"
private const val KEY_TORUS_BASE_SUB_VERIFIER = "KEY_TORUS_BASE_SUB_VERIFIER"
private const val KEY_MOONPAY_SERVER_SIDE_BASE_URL = "KEY_MOONPAY_SERVER_SIDE_BASE_URL"
private const val KEY_BRIDGES_SERVICE_BASE_URL = "KEY_BRIDGES_SERVICE_BASE_URL"
private const val KEY_GATEWAY_BASE_URL = "KEY_GATEWAY_BASE_URL"
private const val KEY_TOKEN_SERVICE_BASE_URL = "KEY_TOKEN_SERVICE_BASE_URL"

class NetworkServicesUrlProvider(
    private val context: Context,
    private val storage: NetworkServicesUrlStorage,
    private val crashLogger: CrashLogger
) {

    fun loadFeeRelayerEnvironment(): FeeRelayerEnvironment {
        val url = storage.getString(
            KEY_FEE_RELAYER_BASE_URL,
            context.getString(R.string.feeRelayerBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_FEE_RELAYER_BASE_URL, url)
        return FeeRelayerEnvironment(url)
    }

    fun saveFeeRelayerEnvironment(newUrl: String) {
        storage.putString(KEY_FEE_RELAYER_BASE_URL, newUrl)
    }

    fun loadNameServiceEnvironment(): NameServiceEnvironment {
        val url = storage.getString(
            KEY_NAME_SERVICE_BASE_URL,
            context.getString(R.string.registerUsernameServiceProductionUrl)
        ).orEmpty()

        val isProductionSelected = url == context.getString(R.string.registerUsernameServiceProductionUrl)

        crashLogger.setCustomKey(KEY_NAME_SERVICE_BASE_URL, url)

        return NameServiceEnvironment(url, isProductionSelected)
    }

    fun toggleNameServiceEnvironment(isProdSelected: Boolean) {
        if (isProdSelected) {
            saveNameServiceEnvironment(context.getString(R.string.registerUsernameServiceProductionUrl))
        } else {
            saveNameServiceEnvironment(context.getString(R.string.registerUsernameServiceTestUrl))
        }
    }

    fun toggleTokenServiceEnvironment(isProdSelected: Boolean) {
        if (isProdSelected) {
            saveNameServiceEnvironment(context.getString(R.string.tokenServiceBaseUrl))
        } else {
            saveNameServiceEnvironment(context.getString(R.string.tokenServiceTestBaseUrl))
        }
    }

    private fun saveNameServiceEnvironment(newUrl: String) {
        storage.putString(KEY_NAME_SERVICE_BASE_URL, newUrl)
    }

    fun loadNotificationServiceEnvironment(): NotificationServiceEnvironment {
        val url = storage.getString(
            KEY_NOTIFICATION_SERVICE_BASE_URL,
            context.getString(R.string.notificationServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_NOTIFICATION_SERVICE_BASE_URL, url)
        return NotificationServiceEnvironment(url)
    }

    fun saveNotificationServiceEnvironment(newUrl: String) {
        storage.putString(KEY_NOTIFICATION_SERVICE_BASE_URL, newUrl)
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

        return GatewayServiceEnvironment(url)
    }

    fun saveGatewayServiceEnvironment(newUrl: String) {
        storage.putString(KEY_GATEWAY_BASE_URL, newUrl)
    }

    fun loadTorusEnvironment(): TorusEnvironment {
        val url = storage.getString(
            KEY_TORUS_BASE_URL,
            context.getString(R.string.torusBaseUrl)
        ).orEmpty()
        val verifier = storage.getString(
            KEY_TORUS_BASE_VERIFIER,
            context.getString(R.string.torusVerifier)
        ).orEmpty()
        Timber.d("Torus verifier: $verifier")

        val subVerifier = storage.getString(
            KEY_TORUS_BASE_SUB_VERIFIER,
            context.getStringResourceByName("torusSubVerifier")
        ).orEmpty()

        if (!BuildConfig.DEBUG && subVerifier.isBlank()) {
            Timber.e(IllegalArgumentException("torusSubVerifier is missing for release builds!"))
        }

        val torusEnvironment = TorusEnvironment(
            baseUrl = url,
            verifier = verifier,
            subVerifier = subVerifier,
            torusNetwork = context.getString(R.string.torusNetwork),
            torusLogLevel = context.getString(R.string.torusLogLevel)
        )

        Timber.i("Torus environment init: $torusEnvironment")
        return torusEnvironment
    }

    fun saveTorusEnvironment(newUrl: String?, newVerifier: String?, newSubVerifier: String?) {
        storage.edit {
            newUrl?.let {
                putString(KEY_TORUS_BASE_URL, it)
            }
            newVerifier?.let {
                putString(KEY_TORUS_BASE_VERIFIER, it)
            }
            newSubVerifier?.let {
                putString(KEY_TORUS_BASE_SUB_VERIFIER, it)
            } ?: run {
                remove(KEY_TORUS_BASE_SUB_VERIFIER)
            }
        }
        Timber.i("Torus environment changed and saved: $newUrl; $newVerifier; $newSubVerifier")
    }

    fun loadMoonpayEnvironment(): MoonpayEnvironment {
        val defaultUrl = if (BuildConfig.DEBUG) {
            context.getString(R.string.moonpayServerSideProxySandboxUrl)
        } else {
            context.getString(R.string.moonpayServerSideProxyUrl)
        }
        val serverSideBaseUrl = storage.getString(KEY_MOONPAY_SERVER_SIDE_BASE_URL, defaultUrl).orEmpty()
        val clientSideBaseUrl = context.getString(R.string.moonpayClientSideBaseUrl)
        val isSandboxEnabled = serverSideBaseUrl == context.getString(R.string.moonpayServerSideProxySandboxUrl)

        crashLogger.setCustomKey(KEY_MOONPAY_SERVER_SIDE_BASE_URL, serverSideBaseUrl)
        crashLogger.setCustomKey("KEY_MOONPAY_CLIENT_SIDE_BASE_URL", clientSideBaseUrl)

        return MoonpayEnvironment(
            baseServerSideUrl = serverSideBaseUrl,
            baseClientSideUrl = clientSideBaseUrl,
            isSandboxEnabled = isSandboxEnabled,
            moonpayApiKey = if (isSandboxEnabled) BuildConfig.moonpaySandboxKey else BuildConfig.moonpayKey
        )
    }

    fun toggleMoonpayEnvironment(isSandboxSelected: Boolean) {
        if (isSandboxSelected) {
            saveMoonpayEnvironment(context.getString(R.string.moonpayServerSideProxySandboxUrl))
        } else {
            saveMoonpayEnvironment(context.getString(R.string.moonpayServerSideProxyUrl))
        }
    }

    fun loadTokenServiceEnvironment(): TokenServiceEnvironment {
        val url = storage.getString(
            KEY_TOKEN_SERVICE_BASE_URL,
            context.getString(R.string.tokenServiceBaseUrl)
        ).orEmpty()

        crashLogger.setCustomKey(KEY_TOKEN_SERVICE_BASE_URL, url)
        return TokenServiceEnvironment(url)
    }

    private fun saveMoonpayEnvironment(newServerSideUrl: String) {
        storage.putString(KEY_MOONPAY_SERVER_SIDE_BASE_URL, newServerSideUrl)
    }

    fun resetMoonpayEnvironment() {
        storage.remove(KEY_MOONPAY_SERVER_SIDE_BASE_URL)
    }
}
