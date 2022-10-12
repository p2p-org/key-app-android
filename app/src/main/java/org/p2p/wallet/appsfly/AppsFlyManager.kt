package org.p2p.wallet.appsfly

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import org.p2p.wallet.BuildConfig
import timber.log.Timber

private const val TAG = "AppsFlyManager"

object AppsFlyManager {

    fun install(application: Application) {
        val appsFlyer = AppsFlyerLib.getInstance().apply {
            setDebugLog(BuildConfig.DEBUG)
        }
        appsFlyer.init(BuildConfig.appsFlyerKey, listener, application)
        appsFlyer.start(application)
    }

    private object listener : AppsFlyerConversionListener {

        override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
            Timber.tag(TAG).d("On Conversion Success: data = $p0")
        }

        override fun onConversionDataFail(p0: String?) {
            Timber.tag(TAG).d("On Conversion Failure: cause = $p0")
        }

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
            Timber.tag(TAG).d("On App open attribution success: data = $p0")
        }

        override fun onAttributionFailure(p0: String?) {
            Timber.tag(TAG).d("On App open attribution failure: cause = $p0")
        }
    }
}
