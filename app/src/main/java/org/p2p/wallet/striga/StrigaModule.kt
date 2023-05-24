package org.p2p.wallet.striga

import org.koin.dsl.module

object StrigaModule {

    fun create() = module {
        includes(
            StrigaSignupModule.create()
        )
    }
}
