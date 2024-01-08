package org.p2p.wallet.common.analytics.interactor

import org.p2p.core.analytics.repository.AnalyticsLocalRepository
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.sell.interactor.SellInteractor
import timber.log.Timber
import kotlinx.coroutines.launch

class ScreensAnalyticsInteractor(
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsLocalRepository: AnalyticsLocalRepository,
    private val sellInteractor: SellInteractor,
    private val appScope: AppScope
) {

    fun logScreenOpenEvent(newScreenName: String) {
        val previousScreenName = getCurrentScreenName()
        analyticsLocalRepository.changeCurrentScreen(newScreenName)

        val isNewScreenOpened = newScreenName != previousScreenName
        if (isNewScreenOpened) {
            appScope.launch {
                Timber.tag("ScreensAnalyticsInteractor").i("logScreenOpened: $newScreenName")
                browseAnalytics.logScreenOpened(
                    screenName = newScreenName,
                    lastScreen = getPreviousScreenName()
                )
            }
        }
    }

    fun getPreviousScreenName(): String = analyticsLocalRepository.getPreviousScreenName()

    fun getCurrentScreenName(): String = analyticsLocalRepository.getCurrentScreenName()
}
