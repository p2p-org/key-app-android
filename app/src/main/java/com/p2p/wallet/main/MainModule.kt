package com.p2p.wallet.main

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.ui.MainContract
import com.p2p.wallet.main.ui.MainPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object MainModule : InjectionModule {

    override fun create() = module {
        factory { MainPresenter(get()) } bind MainContract.Presenter::class
    }
}