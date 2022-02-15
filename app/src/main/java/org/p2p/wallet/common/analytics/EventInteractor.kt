package org.p2p.wallet.common.analytics

class EventInteractor(private val trackerContract: TrackerContract) {

    fun logScreenOpenEvent(screenName: String) {
        trackerContract.logEvent(screenName)
    }
}