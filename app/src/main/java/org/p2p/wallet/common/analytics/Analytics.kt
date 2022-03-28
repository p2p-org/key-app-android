package org.p2p.wallet.common.analytics

import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker

/**
 * Single entry point that behaves like a proxy for all AnalyticsTracker impl`s
 */
class Analytics(private val trackers: Set<AnalyticsTracker>) {

    fun logEvent(event: String, params: Map<String, Any> = emptyMap()) {
        trackers.forEach { it.logEvent(event, params) }
    }

    fun logEvent(event: String, params: Array<out Pair<String, Any>>) {
        trackers.forEach { it.logEvent(event, params) }
    }

    fun setUserProperty(key: String, value: String) {
        trackers.forEach { it.setUserProperty(key, value) }
    }

    fun setUserProperty(key: String, value: Boolean) {
        setUserProperty(key, if (value) "TRUE" else "FALSE")
    }

    fun setUserPropertyOnce(key: String, value: String) {
        trackers.forEach { it.setUserPropertyOnce(key, value) }
    }

    fun incrementUserProperty(property: String, byValue: Int) {
        trackers.forEach { it.incrementUserProperty(property, byValue) }
    }

    fun setUserId(userId: String?) {
        trackers.forEach { it.setUserId(userId) }
    }

    fun appendToArray(property: String, value: Int) {
        trackers.forEach { it.appendToArray(property, value) }
    }
}
