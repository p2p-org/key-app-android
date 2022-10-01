package org.p2p.wallet.solend

import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factory { SolendEarnPresenter(get()) } bind SolendEarnContract.Presenter::class
    }
}
