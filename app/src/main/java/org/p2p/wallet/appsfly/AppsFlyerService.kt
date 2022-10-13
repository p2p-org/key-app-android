package org.p2p.wallet.appsfly

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import org.p2p.wallet.BuildConfig

class AppsFlyerService {

    private val listener: AppsFlyerConversionListener = AppsFlyerConversionListenerImpl()

    fun install(application: Application, devKey: String) {
        val appsFlyer = AppsFlyerLib.getInstance().apply {
            setDebugLog(BuildConfig.DEBUG)
        }
        appsFlyer.init(devKey, listener, application)
        appsFlyer.start(application)
    }
}
