package org.p2p.wallet.common.analytics.interactor

import org.p2p.wallet.common.analytics.repository.AnalyticsLocalRepository
import org.p2p.wallet.home.analytics.BrowseAnalytics

class ScreensAnalyticsInteractor(
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsLocalRepository: AnalyticsLocalRepository
) {

    fun logScreenOpenEvent(newScreenName: String) {
        val previousScreenName = getCurrentScreenName()
        analyticsLocalRepository.changeCurrentScreen(newScreenName)

        val isNewScreenOpened = newScreenName != previousScreenName
        if (isNewScreenOpened) {
            browseAnalytics.logScreenOpened(newScreenName, getPreviousScreenName())
        }
    }

    fun getPreviousScreenName(): String = analyticsLocalRepository.getPreviousScreenName()

    fun getCurrentScreenName(): String = analyticsLocalRepository.getCurrentScreenName()
}
