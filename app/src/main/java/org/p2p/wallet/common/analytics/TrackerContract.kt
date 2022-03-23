package org.p2p.wallet.common.analytics

interface TrackerContract {

    fun logEvent(event: String, params: Array<out Pair<String, Any>>? = null) = Unit

    fun setUserProperty(key: String, value: String) = Unit

    fun incrementUserProperty(property: String, byValue: Int = 1) = Unit

    fun setUserPropertyOnce(key: String, value: String) = Unit

    fun setUserId(userId: String?) = Unit

    fun appendToArray(property: String, value: Int) = Unit

    fun regenerateDeviceId() = Unit
}
