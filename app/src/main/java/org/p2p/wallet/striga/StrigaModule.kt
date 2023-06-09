package org.p2p.wallet.striga

import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.di.StrigaSignupModule

object StrigaModule : InjectionModule {

    override fun create() = module {
        includes(
            StrigaSignupModule.create()
        )
    }
}
