package org.p2p.wallet.striga.offramp

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawContract
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawPresenter
import org.p2p.wallet.striga.offramp.interactor.StrigaOffRampInteractor
import org.p2p.wallet.striga.offramp.interactor.polling.StrigaOffRampExchangeRateNotifier
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampMapper
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampSwapWidgetMapper
import org.p2p.wallet.striga.offramp.ui.StrigaOffRampPresenter

object StrigaOffRampModule : InjectionModule {
    override fun create() = module {
        initDataLayer()
        initDomainLayer()

        factoryOf(::StrigaWithdrawPresenter) bind StrigaWithdrawContract.Presenter::class
        factoryOf(::StrigaOffRampPresenter) bind StrigaOffRampContract.Presenter::class
    }

    private fun Module.initDataLayer() {
        factoryOf(::StrigaOffRampMapper)
        factoryOf(::StrigaOffRampSwapWidgetMapper)
    }

    private fun Module.initDomainLayer() {
        factoryOf(::StrigaOffRampInteractor)
        factoryOf(::StrigaOffRampExchangeRateNotifier)
    }
}
