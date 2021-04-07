package com.p2p.wowlet.root

import com.p2p.wowlet.common.di.InjectionModule
import org.koin.dsl.bind
import org.koin.dsl.module

object RootModule : InjectionModule {

    override fun create() = module {
        factory { RootPresenter(get()) } bind RootContract.Presenter::class
    }
}