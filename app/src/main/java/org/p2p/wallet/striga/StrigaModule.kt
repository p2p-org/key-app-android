package org.p2p.wallet.striga

import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.striga.di.StrigaSignupModule
import org.p2p.wallet.striga.kyc.StrigaKycModule

object StrigaModule : InjectionModule {

    override fun create() = module {
        includes(
            StrigaSignupModule.create(),
            StrigaKycModule.create()
        )
    }
}
