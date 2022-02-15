package org.p2p.wallet.common.analytics

import org.p2p.wallet.common.analytics.repository.HistoryLocalRepository

class EventInteractor(
    private val localRepository: HistoryLocalRepository,
    private val trackerContract: TrackerContract
) {

    fun onScreenChanged(screenName: String) {
        localRepository.saveLastScreenName(screenName)
    }

    fun getLastScreenName(): String = localRepository.getLastScreenName()

    fun logScreenOpenEvent(screenName: String) {
        trackerContract.logEvent(screenName)
    }
}