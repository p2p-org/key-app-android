package org.p2p.wallet.common.analytics.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import timber.log.Timber

class AppsFlyerTracker(
    private val context: Context,
    private val appsFlyer: AppsFlyerLib
) : AnalyticsTracker {

    private val requestListener = object : AppsFlyerRequestListener {
        override fun onSuccess() {
            Timber.tag("AppsFlyerTracker").d("Event successfully logged")
        }

        override fun onError(p0: Int, message: String) {
            Timber.tag("AppsFlyerTracker").e("Error while try to log event: $message")
        }
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        appsFlyer.logEvent(context, eventName, params, requestListener)
    }

    override fun logEvent(eventName: String, params: Array<out Pair<String, Any>>) {
        appsFlyer.logEvent(context, eventName, params.toMap(), requestListener)
    }

    override fun setUserProperty(key: String, value: String) = Unit

    override fun incrementUserProperty(property: String, byValue: Int) = Unit

    override fun setUserPropertyOnce(key: String, value: String) = Unit

    override fun setUserId(userId: String?) = Unit

    override fun appendToArray(property: String, value: Int) = Unit

    override fun regenerateDeviceId() = Unit

    override fun clearUserProperties() = Unit
}
