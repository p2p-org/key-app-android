package org.p2p.wallet.solend

import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.ui.SolendEarnPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factory { SolendEarnPresenter() }
    }
}
