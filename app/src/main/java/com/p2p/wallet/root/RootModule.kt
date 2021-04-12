package com.p2p.wallet.root

import com.p2p.wallet.common.di.InjectionModule
import org.koin.dsl.bind
import org.koin.dsl.module

object RootModule : InjectionModule {

    override fun create() = module {
        factory { RootPresenter(get()) } bind RootContract.Presenter::class
    }
}