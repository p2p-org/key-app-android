package org.p2p.wallet.striga

import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.ui.firststep.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.ui.firststep.StrigaSignUpFirstStepPresenter
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepPresenter
import org.p2p.wallet.striga.di.StrigaSignupModule

object StrigaModule : InjectionModule {

    override fun create() = module {
        includes(
            StrigaSignupModule.create()
        )
    }
}
