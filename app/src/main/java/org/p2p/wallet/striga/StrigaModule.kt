package org.p2p.wallet.striga

import org.koin.dsl.module
import org.p2p.wallet.striga.di.StrigaSignupModule

object StrigaModule {

    fun create() = module {
        includes(
            StrigaSignupModule.create()
        )
    }
}
