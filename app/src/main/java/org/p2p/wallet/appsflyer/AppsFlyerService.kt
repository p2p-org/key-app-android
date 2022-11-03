package org.p2p.wallet.appsflyer

import android.app.Application
import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkResult
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

private const val UNINSTALL_APP_KEY = "af-uinstall-tracking"
private const val TAG = "AppsFlyerService"

class AppsFlyerService(private val context: Context) {

    private val listener: AppsFlyerConversionListener = AppsFlyerConversionListenerImpl()

    fun install(application: Application, devKey: String) {
        val appsFlyer = AppsFlyerLib.getInstance()
        kotlin.runCatching {
            appsFlyer.setDebugLog(false) // turn to true if needed
            appsFlyer.init(devKey, listener, application)
            appsFlyer.start(application)
            setupListeners()
        }.onSuccess {
            Timber.tag(TAG).i("AppsFlyer service is initialized")
        }.onFailure {
            Timber.tag(TAG).i("AppsFlyer service, error on init  = $it")
        }
    }

    fun onNewToken(newToken: String) {
        val appsFlyerInstance = AppsFlyerLib.getInstance()
        // Sending new token to AppsFlyer
        appsFlyerInstance.updateServerUninstallToken(context, newToken)
        // the rest of the code that makes use of the token goes in this method as well
    }

    fun isUninstallTrackingMessage(message: RemoteMessage): Boolean {
        val data = message.data
        return data.containsKey(UNINSTALL_APP_KEY)
    }

    private fun setupListeners() {
        val appsFlyerInstance = AppsFlyerLib.getInstance()
        appsFlyerInstance.subscribeForDeepLink { result ->
            val status = result.status
            when (status) {
                DeepLinkResult.Status.FOUND -> {
                    val deeplink = result.deepLink
                    Timber.tag(TAG).i("Deeplink found = ${deeplink.values}")
                }
                DeepLinkResult.Status.NOT_FOUND -> {
                    Timber.tag(TAG).i("Deeplink not found")
                }
                DeepLinkResult.Status.ERROR -> {
                    Timber.tag(TAG).i("Error on fetch deeplink = ${result.error.name}")
                }
            }
        }
    }
}
