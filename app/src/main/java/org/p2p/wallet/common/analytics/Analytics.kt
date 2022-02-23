package org.p2p.wallet.common.analytics

class Analytics(
    private val trackers: Set<TrackerContract>
) : TrackerContract {

    override fun logEvent(event: String, params: Array<out Pair<String, Any>>?) {
        trackers.forEach { it.logEvent(event, params) }
    }

    override fun setUserProperty(key: String, value: String) {
        trackers.forEach { it.setUserProperty(key, value) }
    }

    fun setUserProperty(key: String, value: Boolean) {
        setUserProperty(key, if (value) "TRUE" else "FALSE")
    }

    override fun setUserPropertyOnce(key: String, value: String) {
        trackers.forEach { it.setUserPropertyOnce(key, value) }
    }

    override fun incrementUserProperty(property: String, byValue: Int) {
        trackers.forEach { it.incrementUserProperty(property, byValue) }
    }

    override fun setUserId(userId: String?) {
        trackers.forEach { it.setUserId(userId) }
    }

    override fun appendToArray(property: String, value: Int) {
        trackers.forEach { it.appendToArray(property, value) }
    }
}