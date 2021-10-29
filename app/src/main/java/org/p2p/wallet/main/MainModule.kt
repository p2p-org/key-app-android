package org.p2p.wallet.main

import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.main.interactor.SendInteractor
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.repository.MainDatabaseRepository
import org.p2p.wallet.main.repository.MainLocalRepository
import org.p2p.wallet.main.ui.buy.BuyContract
import org.p2p.wallet.main.ui.buy.BuyPresenter
import org.p2p.wallet.main.ui.main.MainContract
import org.p2p.wallet.main.ui.main.MainPresenter
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaPresenter
import org.p2p.wallet.main.ui.send.SendContract
import org.p2p.wallet.main.ui.send.SendPresenter
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
            SendPresenter(token, get(), get(), get(), get(), get())
        } bind SendContract.Presenter::class
        factory { (token: Token.Active?) -> BuyPresenter(token, get(), get()) } bind BuyContract.Presenter::class
    }
}