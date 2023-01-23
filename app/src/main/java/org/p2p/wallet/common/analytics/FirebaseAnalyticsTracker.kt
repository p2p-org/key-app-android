package org.p2p.wallet.common.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        firebaseAnalytics.logEvent(eventName) {
            params.forEach { (key, value) -> param(key, value.toString()) }
        }
    }

    override fun logEvent(eventName: String, params: Array<out Pair<String, Any>>) {
        firebaseAnalytics.logEvent(eventName) {
            params.forEach { (key, value) -> param(key, value.toString()) }
        }
    }

    override fun setUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }

    override fun incrementUserProperty(property: String, byValue: Int) = Unit

    override fun setUserPropertyOnce(key: String, value: String) = Unit

    override fun setUserId(userId: String?) = Unit

    override fun appendToArray(property: String, value: Int) = Unit

    override fun regenerateDeviceId() = Unit

    override fun clearUserProperties() = Unit
}
