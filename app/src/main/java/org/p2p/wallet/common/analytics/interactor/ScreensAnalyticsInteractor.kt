package org.p2p.wallet.common.analytics.interactor

import org.p2p.wallet.common.analytics.repository.AnalyticsLocalRepository
import org.p2p.wallet.home.analytics.BrowseAnalytics

class ScreensAnalyticsInteractor(
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsLocalRepository: AnalyticsLocalRepository
) {

    fun logScreenOpenEvent(screenName: String) {
        browseAnalytics.logScreenOpened(screenName, getPreviousScreenName())
        analyticsLocalRepository.onScreenChanged(screenName)
    }

    fun getPreviousScreenName(): String = analyticsLocalRepository.getPreviousScreenName()

    fun getCurrentScreenName(): String = analyticsLocalRepository.getCurrentScreenName()
}
