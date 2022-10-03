package org.p2p.wallet.solend

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsContract
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsPresenter
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SolendEarnPresenter) bind SolendEarnContract.Presenter::class
        factoryOf(::SolendUserDepositsPresenter) bind SolendUserDepositsContract.Presenter::class
    }
}
