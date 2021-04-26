package com.p2p.wallet.main

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.repository.MainInMemoryRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.ui.main.MainContract
import com.p2p.wallet.main.ui.main.MainPresenter
import com.p2p.wallet.main.ui.receive.ReceiveContract
import com.p2p.wallet.main.ui.receive.ReceivePresenter
import com.p2p.wallet.main.ui.swap.SwapContract
import com.p2p.wallet.main.ui.swap.SwapPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object MainModule : InjectionModule {

    override fun create() = module {
        single { MainInMemoryRepository() } bind MainLocalRepository::class
        single { MainPresenter(get()) } bind MainContract.Presenter::class

        factory { SwapPresenter() } bind SwapContract.Presenter::class
        factory { ReceivePresenter() } bind ReceiveContract.Presenter::class
    }
}