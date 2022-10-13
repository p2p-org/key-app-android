package org.p2p.wallet.appsfly

import android.app.Application
import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.messaging.RemoteMessage
import org.p2p.wallet.BuildConfig

class AppsFlyerService(private val context: Context) {

    private val listener: AppsFlyerConversionListener = AppsFlyerConversionListenerImpl()

    fun install(application: Application, devKey: String) {
        val appsFlyer = AppsFlyerLib.getInstance().apply {
            setDebugLog(BuildConfig.DEBUG)
        }
        appsFlyer.init(devKey, listener, application)
        appsFlyer.start(application)
    }

    fun onNewToken(newToken: String) {
        // Sending new token to AppsFlyer
        AppsFlyerLib.getInstance().updateServerUninstallToken(context, newToken)
        // the rest of the code that makes use of the token goes in this method as well
    }

    fun isUninstallTrackingMessage(message: RemoteMessage): Boolean {
        return message.data.containsKey("af-uinstall-tracking")
    }
}
