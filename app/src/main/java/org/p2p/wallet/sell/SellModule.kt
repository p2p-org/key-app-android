package org.p2p.wallet.sell

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.sell.interactor.SellInteractor

object SellModule : InjectionModule {
    override fun create() = module {
        factoryOf(::SellInteractor)
    }
}
