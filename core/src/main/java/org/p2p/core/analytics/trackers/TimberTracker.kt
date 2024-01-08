package org.p2p.core.analytics.trackers

import timber.log.Timber

private const val TAG_ANALYTICS = "Analytics"

class TimberTracker : AnalyticsTracker {

    override fun setUserProperty(key: String, value: String) {
        Timber.tag(TAG_ANALYTICS).v("setUserProperty() key [$key], value [$value]")
    }

    override fun setUserPropertyOnce(key: String, value: String) {
        Timber.tag(TAG_ANALYTICS).v("setUserPropertyOnce() key [$key], value [$value]")
    }

    override fun logEvent(eventName: String, params: Array<out Pair<String, Any>>) {
        Timber.tag(TAG_ANALYTICS).v("logEvent() event [$eventName], params [${params.toMap()}]")
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        Timber.tag(TAG_ANALYTICS).v("logEvent() event [$eventName], params [$params]")
    }

    override fun incrementUserProperty(property: String, byValue: Int) {
        Timber.tag(TAG_ANALYTICS).v("incrementUserProperty() property [$property] by $byValue")
    }

    override fun setUserId(userId: String?) {
        Timber.tag(TAG_ANALYTICS).v("setUserId($userId)")
    }

    override fun appendToArray(property: String, value: Int) {
        Timber.tag(TAG_ANALYTICS).v("appendToArray() property [$property] value $value")
    }

    override fun regenerateDeviceId() = Unit

    override fun clearUserProperties() = Unit
}
