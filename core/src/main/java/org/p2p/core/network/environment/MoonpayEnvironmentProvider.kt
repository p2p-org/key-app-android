package org.p2p.core.network.environment

import android.content.Context
import org.p2p.core.BuildConfig
import org.p2p.core.R
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.network.NetworkServicesUrlStorage

class MoonpayEnvironmentProvider(
    private val context: Context,
    private val storage: NetworkServicesUrlStorage,
    private val crashLogger: CrashLogger
) {
    private companion object {
        private const val KEY_MOONPAY_SERVER_SIDE_BASE_URL = "KEY_MOONPAY_SERVER_SIDE_BASE_URL"
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

    private fun saveMoonpayEnvironment(newServerSideUrl: String) {
        storage.putString(KEY_MOONPAY_SERVER_SIDE_BASE_URL, newServerSideUrl)
    }

    fun resetMoonpayEnvironment() {
        storage.remove(KEY_MOONPAY_SERVER_SIDE_BASE_URL)
    }
}
