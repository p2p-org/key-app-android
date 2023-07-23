package org.p2p.wallet.striga.offramp

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawContract
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawPresenter

object StrigaOffRampModule : InjectionModule {
    override fun create() = module {
        factoryOf(::StrigaWithdrawPresenter) bind StrigaWithdrawContract.Presenter::class
    }
}
