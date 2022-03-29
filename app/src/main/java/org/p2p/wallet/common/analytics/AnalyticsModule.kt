package org.p2p.wallet.common.analytics

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.common.analytics.repository.AnalyticsInMemoryRepository
import org.p2p.wallet.common.analytics.repository.AnalyticsLocalRepository
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics

object AnalyticsModule : InjectionModule {

    override fun create(): Module = module {
        factory { AdminAnalytics(get()) }
        factory { AuthAnalytics(get()) }
        factory { ReceiveAnalytics(get()) }
        factory { BuyAnalytics(get()) }
        factory { SwapAnalytics(get()) }
        factory { ScreensAnalyticsInteractor(get(), get()) }
        factory { OnBoardingAnalytics(get()) }
        factory { BrowseAnalytics(get()) }
        factory { SendAnalytics(get()) }
        single { AnalyticsInMemoryRepository() } bind AnalyticsLocalRepository::class
    }
}
