package com.p2p.wallet.main

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.interactor.SendInteractor
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.repository.MainDatabaseRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.ui.buy.BuyContract
import com.p2p.wallet.main.ui.buy.BuyPresenter
import com.p2p.wallet.main.ui.main.MainContract
import com.p2p.wallet.main.ui.main.MainPresenter
import com.p2p.wallet.main.ui.receive.solana.ReceiveSolanaContract
import com.p2p.wallet.main.ui.receive.solana.ReceiveSolanaPresenter
import com.p2p.wallet.main.ui.send.SendContract
import com.p2p.wallet.main.ui.send.SendPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object MainModule : InjectionModule {

    override fun create() = module {
        factory { MainDatabaseRepository(get()) } bind MainLocalRepository::class

        /* Cached data exists, therefore creating singleton */
        single { MainPresenter(get(), get()) } bind MainContract.Presenter::class
        factory { SendInteractor(get(), get(), get(), get()) }

        factory { (token: Token.Active?) ->
            ReceiveSolanaPresenter(token, get(), get())
        } bind ReceiveSolanaContract.Presenter::class
        factory { (token: Token.Active) ->
            SendPresenter(token, get(), get(), get())
        } bind SendContract.Presenter::class
        factory { (token: Token.Active?) -> BuyPresenter(token, get(), get()) } bind BuyContract.Presenter::class
    }
}