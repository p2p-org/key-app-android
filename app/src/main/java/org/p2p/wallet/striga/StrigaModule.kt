package org.p2p.wallet.striga

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.di.StrigaSignupModule
import org.p2p.wallet.striga.ui.personaldata.StrigaPersonalInfoContract
import org.p2p.wallet.striga.ui.personaldata.StrigaPersonalInfoPresenter

object StrigaModule : InjectionModule {

    override fun create(): Module = module {
        factoryOf(::StrigaPersonalInfoPresenter) bind StrigaPersonalInfoContract.Presenter::class

        includes(StrigaSignupModule.create())
    }
}
