package org.p2p.core.analytics

import org.p2p.core.analytics.trackers.AnalyticsTracker

/**
 * Single entry point that behaves like a proxy for all AnalyticsTracker impl`s
 */
class Analytics(
    private val trackers: Set<AnalyticsTracker>,
    isDebugBuild: Boolean
) {

    private val shouldAddDebugSuffix: Boolean = isDebugBuild

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
        setUserProperty(key, if (value) "true" else "false")
    }

    fun setUserPropertyOnce(key: String, value: String) {
        trackers.forEach { it.setUserPropertyOnce(key, value) }
    }

    fun setUserPropertyOnce(key: String, value: Boolean) {
        setUserPropertyOnce(key, if (value) "true" else "false")
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

    fun clearUserProperties() {
        trackers.forEach { it.clearUserProperties() }
    }
}
