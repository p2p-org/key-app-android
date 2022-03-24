package org.p2p.wallet.common.analytics.trackers

import android.app.Application
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import org.json.JSONObject
import org.p2p.wallet.BuildConfig
import timber.log.Timber

class AmplitudeTracker(app: Application) : AnalyticsTracker {

    private val amplitude =
        Amplitude.getInstance()
            .initialize(app, BuildConfig.amplitudeKey)
            .trackSessionEvents(true)
            .enableForegroundTracking(app)
            .setMinTimeBetweenSessionsMillis((30 * 60 * 1000).toLong())

    override fun setUserProperty(key: String, value: String) {
        val userProperties = JSONObject()
        userProperties.put(key, value)

        amplitude.setUserProperties(userProperties)
    }

    override fun setUserPropertyOnce(key: String, value: String) {
        amplitude.identify(Identify().setOnce(key, value))
    }

    override fun logEvent(event: String, params: Map<String, Any>) {
        if (params.isEmpty()) {
            amplitude.logEvent(event)
            return
        }
        try {
            amplitude.logEvent(event, JSONObject(params))
        } catch (e: NullPointerException) {
            Timber.w(e, "Unable to put key - value into json")
        }
    }

    override fun logEvent(event: String, params: Array<out Pair<String, Any>>) {
        logEvent(event, params.toMap())
    }

    override fun incrementUserProperty(property: String, byValue: Int) {
        amplitude.identify(Identify().add(property, byValue))
    }

    override fun appendToArray(property: String, value: Int) {
        amplitude.identify(Identify().append(property, value))
    }

    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId, false)
    }

    override fun regenerateDeviceId() {
        amplitude.regenerateDeviceId()
    }
}
