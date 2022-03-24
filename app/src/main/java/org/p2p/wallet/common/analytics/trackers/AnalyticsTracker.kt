package org.p2p.wallet.common.analytics.trackers

interface AnalyticsTracker {
    fun logEvent(event: String, params: Map<String, Any> = emptyMap())
    fun logEvent(event: String, params: Array<out Pair<String, Any>>)
    fun setUserProperty(key: String, value: String)
    fun incrementUserProperty(property: String, byValue: Int = 1)
    fun setUserPropertyOnce(key: String, value: String)
    fun setUserId(userId: String?)
    fun appendToArray(property: String, value: Int)
    fun regenerateDeviceId()
}
