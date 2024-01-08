package org.p2p.core.analytics.trackers

import android.app.Application
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val MIN_TIME_BETWEEN_SESSIONS_MIN = 30L

class AmplitudeTracker(
    app: Application,
    apiKey: String
) : AnalyticsTracker {

    private val amplitude: AmplitudeClient =
        Amplitude.getInstance()
            .initialize(app, apiKey)
            .trackSessionEvents(true)
            .enableForegroundTracking(app)
            .setMinTimeBetweenSessionsMillis(TimeUnit.MINUTES.toMillis(MIN_TIME_BETWEEN_SESSIONS_MIN))

    override fun setUserProperty(key: String, value: String) {
        val userProperties = JSONObject()
        userProperties.put(key, value)

        amplitude.setUserProperties(userProperties)
    }

    override fun setUserPropertyOnce(key: String, value: String) {
        amplitude.identify(Identify().set(key, value))
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        if (params.isEmpty()) {
            amplitude.logEvent(eventName)
            return
        }
        try {
            amplitude.logEvent(eventName, JSONObject(mapParamsToAnalyticValues(params)))
            Timber.d("logEvent($eventName) event sent using Amplitude")
        } catch (e: NullPointerException) {
            Timber.w(e, "Unable to put key - value into json")
        }
    }

    private fun mapParamsToAnalyticValues(params: Map<String, Any>): Map<String, Any> {
        return params.mapValues { (_, value) ->
            if (value is Boolean) {
                if (value) "True" else "False"
            } else {
                value
            }
        }
    }

    override fun logEvent(eventName: String, params: Array<out Pair<String, Any>>) {
        logEvent(eventName, params.toMap())
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

    override fun clearUserProperties() {
        amplitude.clearUserProperties()
    }
}
