package com.p2p.wallet.main

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.repository.MainInMemoryRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.repository.MainRemoteRepository
import com.p2p.wallet.main.repository.MainRepository
import com.p2p.wallet.main.ui.main.MainContract
import com.p2p.wallet.main.ui.main.MainPresenter
import com.p2p.wallet.main.ui.receive.ReceiveContract
import com.p2p.wallet.main.ui.receive.ReceivePresenter
import com.p2p.wallet.main.ui.send.SendContract
import com.p2p.wallet.main.ui.send.SendPresenter
import com.p2p.wallet.swap.ui.SwapContract
import com.p2p.wallet.swap.ui.SwapPresenter
import com.p2p.wallet.token.model.Token
import org.koin.dsl.bind
import org.koin.dsl.module

object MainModule : InjectionModule {

    override fun create() = module {
        single { MainInteractor(get(), get()) }
        single { MainInMemoryRepository() } bind MainLocalRepository::class
        single { MainRemoteRepository(get(), get()) } bind MainRepository::class
        single { MainPresenter(get(), get()) } bind MainContract.Presenter::class

        factory { SwapPresenter(get(), get()) } bind SwapContract.Presenter::class
        factory { (token: Token?) ->
            ReceivePresenter(
                token,
                get(),
                get(),
                get()
            )
        } bind ReceiveContract.Presenter::class
        factory { SendPresenter(get(), get()) } bind SendContract.Presenter::class
    }
}