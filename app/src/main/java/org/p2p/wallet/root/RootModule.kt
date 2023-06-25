package org.p2p.wallet.root

import org.koin.core.module.dsl.factoryOf
import org.p2p.core.common.di.InjectionModule
import org.koin.dsl.bind
import org.koin.dsl.module

object RootModule : InjectionModule {

    override fun create() = module {
        factoryOf(::RootPresenter) bind RootContract.Presenter::class
    }
}
