package org.p2p.wallet.claim

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.claim.ui.ClaimContract
import org.p2p.wallet.claim.ui.ClaimPresenter
import org.p2p.wallet.common.di.InjectionModule

object ClaimModule : InjectionModule {

    override fun create() = module {
        factoryOf(::ClaimPresenter) bind ClaimContract.Presenter::class
    }
}
