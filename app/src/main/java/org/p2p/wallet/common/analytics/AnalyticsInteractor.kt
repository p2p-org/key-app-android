package org.p2p.wallet.common.analytics

import org.p2p.wallet.auth.analytics.repository.AnalyticsLocalRepository

class AnalyticsInteractor(
    private val trackerContract: TrackerContract,
    private val analyticsLocalRepository: AnalyticsLocalRepository
) {

    fun logScreenOpenEvent(screenName: String) {
        trackerContract.logEvent(screenName)
    }

    fun getPreviousScreenName() = analyticsLocalRepository.getPreviousScreenName()

    fun getCurrentScreenName() = analyticsLocalRepository.getCurrentScreenName()
}