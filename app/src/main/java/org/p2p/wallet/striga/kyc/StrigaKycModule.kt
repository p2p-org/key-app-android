package org.p2p.wallet.striga.kyc

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.kyc.ui.StrigaKycContract
import org.p2p.wallet.striga.kyc.ui.StrigaKycInteractor
import org.p2p.wallet.striga.kyc.ui.StrigaKycPresenter

object StrigaKycModule : InjectionModule {
    override fun create() = module {
        factoryOf(::StrigaKycInteractor)
        factoryOf(::StrigaKycPresenter) bind StrigaKycContract.Presenter::class
    }
}
