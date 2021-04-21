package com.p2p.wallet.main

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.repository.MainRepository
import com.p2p.wallet.main.repository.MainRepositoryImpl
import com.p2p.wallet.main.ui.main.MainContract
import com.p2p.wallet.main.ui.main.MainPresenter
import com.p2p.wallet.main.ui.swap.SwapContract
import com.p2p.wallet.main.ui.swap.SwapPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object MainModule : InjectionModule {

    override fun create() = module {
        factory { MainPresenter(get()) } bind MainContract.Presenter::class
        factory { SwapPresenter() } bind SwapContract.Presenter::class
        single { MainRepositoryImpl(get()) } bind MainRepository::class
    }
}