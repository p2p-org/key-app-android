package org.p2p.wallet.striga

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.ui.firststep.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.ui.firststep.StrigaSignUpFirstStepPresenter
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepPresenter

object StrigaModule : InjectionModule {

    override fun create(): Module = module {
        factoryOf(::StrigaSignUpFirstStepPresenter) bind StrigaSignUpFirstStepContract.Presenter::class
        factoryOf(::StrigaSignUpSecondStepPresenter) bind StrigaSignUpSecondStepContract.Presenter::class
    }
}
