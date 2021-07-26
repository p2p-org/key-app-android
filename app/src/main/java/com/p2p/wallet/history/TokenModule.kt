package com.p2p.wallet.history

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.history.interactor.TokenInteractor
import com.p2p.wallet.history.repository.TokenRemoteRepository
import com.p2p.wallet.history.repository.TokenRepository
import com.p2p.wallet.history.ui.main.TokenDetailsContract
import com.p2p.wallet.history.ui.main.TokenDetailsPresenter
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

object TokenModule : InjectionModule {

    override fun create(): Module = module {

        factory { TokenRemoteRepository(get()) } bind TokenRepository::class
        factory { TokenInteractor(get()) }
        factory { TokenDetailsPresenter(get(), get(), get()) } bind TokenDetailsContract.Presenter::class
    }
}