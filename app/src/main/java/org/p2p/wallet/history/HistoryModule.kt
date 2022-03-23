package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.repository.HistoryRemoteRepository
import org.p2p.wallet.history.repository.HistoryRepository
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.home.model.Token

object HistoryModule : InjectionModule {

    override fun create(): Module = module {

        factory { HistoryRemoteRepository(get()) } bind HistoryRepository::class
        factory { HistoryInteractor(get(), get(), get(), get(), get()) }
        factory { (token: Token.Active) ->
            HistoryPresenter(
                token,
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        } bind HistoryContract.Presenter::class
        factory { (state: TransactionDetailsLaunchState) ->
            TransactionDetailsPresenter(state, get(), get(), get())
        } bind TransactionDetailsContract.Presenter::class
    }
}
