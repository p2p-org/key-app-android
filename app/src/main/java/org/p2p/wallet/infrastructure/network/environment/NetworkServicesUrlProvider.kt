package org.p2p.wallet.infrastructure.network.environment

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.utils.getStringResourceByName
import timber.log.Timber

private const val KEY_NOTIFICATION_SERVICE_BASE_URL = "KEY_NOTIFICATION_SERVICE_BASE_URL"
private const val KEY_FEE_RELAYER_BASE_URL = "KEY_FEE_RELAYER_BASE_URL"
private const val KEY_NAME_SERVICE_BASE_URL = "KEY_NAME_SERVICE_BASE_URL"
private const val KEY_TORUS_BASE_URL = "KEY_TORUS_BASE_URL"
private const val KEY_TORUS_BASE_VERIFIER = "KEY_TORUS_BASE_VERIFIER"
private const val KEY_TORUS_BASE_SUB_VERIFIER = "KEY_TORUS_BASE_SUB_VERIFIER"
private const val KEY_MOONPAY_SERVER_SIDE_BASE_URL = "KEY_MOONPAY_SERVER_SIDE_BASE_URL"

class NetworkServicesUrlProvider(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    fun loadFeeRelayerEnvironment(): FeeRelayerEnvironment {
        val url = sharedPreferences.getString(
            KEY_FEE_RELAYER_BASE_URL,
            context.getString(R.string.feeRelayerBaseUrl)
        ).orEmpty()

        return FeeRelayerEnvironment(url)
    }

    fun saveFeeRelayerEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_FEE_RELAYER_BASE_URL, newUrl) }
    }

    fun loadNameServiceEnvironment(): NameServiceEnvironment {
        val url = sharedPreferences.getString(
            KEY_NAME_SERVICE_BASE_URL,
            context.getString(R.string.registerUsernameServiceProductionUrl)
        ).orEmpty()

        return NameServiceEnvironment(url)
    }

    fun saveNameServiceEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_NAME_SERVICE_BASE_URL, newUrl) }
    }

    fun loadNotificationServiceEnvironment(): NotificationServiceEnvironment {
        val url = sharedPreferences.getString(
            KEY_NOTIFICATION_SERVICE_BASE_URL,
            context.getString(R.string.notificationServiceBaseUrl)
        ).orEmpty()

        return NotificationServiceEnvironment(url)
    }

    fun saveNotificationServiceEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_NOTIFICATION_SERVICE_BASE_URL, newUrl) }
    }

    fun loadTorusEnvironment(): TorusEnvironment {
        val url = sharedPreferences.getString(
            KEY_TORUS_BASE_URL,
            context.getString(R.string.torusBaseUrl)
        ).orEmpty()
        val verifier = sharedPreferences.getString(
            KEY_TORUS_BASE_VERIFIER,
            context.getString(R.string.torusVerifier)
        ).orEmpty()

        val subVerifier = sharedPreferences.getString(
            KEY_TORUS_BASE_SUB_VERIFIER,
            context.getStringResourceByName("torusSubVerifier")
        ).orEmpty()

        if (!BuildConfig.DEBUG && subVerifier.isBlank()) {
            Timber.e(IllegalArgumentException("torusSubVerifier is missing for release builds!"))
        }

        val torusEnvironment = TorusEnvironment(url, verifier, subVerifier)

        Timber.i("Torus environment init: $torusEnvironment")
        return torusEnvironment
    }

    fun saveTorusEnvironment(newUrl: String?, newVerifier: String?, newSubVerifier: String?) {
        sharedPreferences.edit {
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
        Timber.i("Torus environment changed and saved: $newUrl;$newVerifier;$newSubVerifier")
    }

    fun loadMoonpayEnvironment(): MoonpayEnvironment {
        val defaultUrl = if (BuildConfig.DEBUG) {
            context.getString(R.string.moonpayServerSideProxySandboxUrl)
        } else {
            context.getString(R.string.moonpayServerSideProxyUrl)
        }
        val serverSideBaseUrl = sharedPreferences.getString(KEY_MOONPAY_SERVER_SIDE_BASE_URL, defaultUrl).orEmpty()
        val clientSideBaseUrl = context.getString(R.string.moonpayClientSideBaseUrl)
        val isSandboxEnabled = serverSideBaseUrl == context.getString(R.string.moonpayServerSideProxySandboxUrl)

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

    private fun saveMoonpayEnvironment(newServerSideUrl: String) {
        sharedPreferences.edit { putString(KEY_MOONPAY_SERVER_SIDE_BASE_URL, newServerSideUrl) }
    }

    fun resetMoonpayEnvironment() {
        sharedPreferences.edit { remove(KEY_MOONPAY_SERVER_SIDE_BASE_URL) }
    }
}
