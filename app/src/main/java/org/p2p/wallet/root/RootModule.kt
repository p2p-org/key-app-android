package org.p2p.wallet.root

import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.root.ui.RootContract
import org.p2p.wallet.root.ui.RootPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object RootModule : InjectionModule {

    override fun create() = module {
        factory { RootPresenter(get(), get()) } bind RootContract.Presenter::class
    }
}