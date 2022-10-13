package org.p2p.wallet.appsfly

import android.app.Application
import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.messaging.RemoteMessage
import org.p2p.wallet.BuildConfig
import timber.log.Timber

private const val UNINSTALL_APP_KEY = "af-uinstall-tracking"
private const val TAG = "AppsFlyerService"

class AppsFlyerService(private val context: Context) {

    private val listener: AppsFlyerConversionListener = AppsFlyerConversionListenerImpl()

    fun install(application: Application, devKey: String) {
        val appsFlyer = AppsFlyerLib.getInstance()
        kotlin.runCatching {
            appsFlyer.setDebugLog(BuildConfig.DEBUG)
            appsFlyer.init(devKey, listener, application)
            appsFlyer.start(application)
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
}
