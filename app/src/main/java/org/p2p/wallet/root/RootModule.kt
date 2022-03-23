package org.p2p.wallet.root

import org.p2p.wallet.common.di.InjectionModule
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.analytics.AdminAnalytics

object RootModule : InjectionModule {

    override fun create() = module {
        factory { RootPresenter(get(), get()) } bind RootContract.Presenter::class
        single { AdminAnalytics(get()) }
    }
}
