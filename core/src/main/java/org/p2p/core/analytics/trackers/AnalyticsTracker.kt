package org.p2p.core.analytics.trackers

interface AnalyticsTracker {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun logEvent(eventName: String, params: Array<out Pair<String, Any>>)
    fun setUserProperty(key: String, value: String)
    fun incrementUserProperty(property: String, byValue: Int = 1)
    fun setUserPropertyOnce(key: String, value: String)
    fun setUserId(userId: String?)
    fun appendToArray(property: String, value: Int)
    fun regenerateDeviceId()
    fun clearUserProperties()
}
