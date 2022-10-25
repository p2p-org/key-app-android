package org.p2p.wallet.common.analytics

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.auth.analytics.GeneralAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.RenBtcAnalytics
import org.p2p.wallet.auth.analytics.UsernameAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.repository.AnalyticsInMemoryRepository
import org.p2p.wallet.common.analytics.repository.AnalyticsLocalRepository
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics

object AnalyticsModule : InjectionModule {
    override fun create() = module {
        single {
            val trackers = TrackersFactory.create(
                app = androidApplication(),
                tokenKeyProvider = get()
            )
            Analytics(trackers)
        }

        factoryOf(::AdminAnalytics)
        factoryOf(::GeneralAnalytics)
        factoryOf(::AuthAnalytics)
        factoryOf(::ReceiveAnalytics)
        factoryOf(::BuyAnalytics)
        factoryOf(::SwapAnalytics)
        factoryOf(::ScreensAnalyticsInteractor)
        factoryOf(::OnboardingAnalytics)
        factoryOf(::BrowseAnalytics)
        factoryOf(::SendAnalytics)
        factoryOf(::HomeAnalytics)
        factoryOf(::UsernameAnalytics)
        factoryOf(::RenBtcAnalytics)

        singleOf(::AnalyticsInMemoryRepository) bind AnalyticsLocalRepository::class
    }
}
