package org.p2p.wallet.main

import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.main.interactor.SearchInteractor
import org.p2p.wallet.main.interactor.SendInteractor
import org.p2p.wallet.main.model.SearchResult
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.repository.MainDatabaseRepository
import org.p2p.wallet.main.repository.MainLocalRepository
import org.p2p.wallet.main.ui.buy.moonpay.BuySolanaContract
import org.p2p.wallet.main.ui.buy.moonpay.BuySolanaPresenter
import org.p2p.wallet.main.ui.main.MainContract
import org.p2p.wallet.main.ui.main.MainPresenter
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaPresenter
import org.p2p.wallet.main.ui.send.SendContract
import org.p2p.wallet.main.ui.send.SendPresenter
import org.p2p.wallet.main.ui.send.search.SearchContract
import org.p2p.wallet.main.ui.send.search.SearchPresenter

object MainModule : InjectionModule {

    override fun create() = module {
        factory { MainDatabaseRepository(get()) } bind MainLocalRepository::class

        /* Cached data exists, therefore creating singleton */
        single { MainPresenter(get(), get(), get(), get(), get()) } bind MainContract.Presenter::class
        factory { SendInteractor(get(), get(), get(), get()) }
        factory { SearchInteractor(get(), get()) }

        factory { (token: Token.Active?) ->
            ReceiveSolanaPresenter(token, get(), get(), get(), get())
        } bind ReceiveSolanaContract.Presenter::class
        factory { (token: Token.Active) ->
            SendPresenter(token, get(), get(), get(), get(), get())
        } bind SendContract.Presenter::class
        factory { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames, get())
        } bind SearchContract.Presenter::class
        factory { BuySolanaPresenter(get(), get()) } bind BuySolanaContract.Presenter::class
    }
}