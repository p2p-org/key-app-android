package org.p2p.wallet.common.analytics

import org.p2p.wallet.utils.NoOp

interface TrackerContract {
    fun logEvent(event: String, params: Map<String, Any> = emptyMap()) = NoOp
    fun logEvent(event: String, params: Array<out Pair<String, Any>>) = NoOp
    fun setUserProperty(key: String, value: String) = NoOp
    fun incrementUserProperty(property: String, byValue: Int = 1) = NoOp
    fun setUserPropertyOnce(key: String, value: String) = NoOp
    fun setUserId(userId: String?) = NoOp
    fun appendToArray(property: String, value: Int) = NoOp
    fun regenerateDeviceId() = NoOp
}
