package org.p2p.wallet.common.analytics

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker

/**
 * Single entry point that behaves like a proxy for all AnalyticsTracker impl`s
 */
class Analytics(private val trackers: Set<AnalyticsTracker>) {

    private val shouldAddDebugSuffix: Boolean = BuildConfig.DEBUG

    fun logEvent(event: String, params: Map<String, Any> = emptyMap()) {
        val modifiedEventName = if (shouldAddDebugSuffix) "${event}_debug" else event
        trackers.forEach { it.logEvent(modifiedEventName, params) }
    }

    fun logEvent(event: String, params: Array<out Pair<String, Any>>) {
        val modifiedEventName = if (shouldAddDebugSuffix) "${event}_debug" else event
        trackers.forEach { it.logEvent(modifiedEventName, params) }
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
