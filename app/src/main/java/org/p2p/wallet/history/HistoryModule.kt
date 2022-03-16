package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParser
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.HistoryTransactionMapper
import org.p2p.wallet.history.interactor.TransactionDetailsMapper
import org.p2p.wallet.history.interactor.TransactionsHistoryRepository
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
        dataLayer()

        factory {
            HistoryInteractor(
                rpcRepository = get(),
                historyTransactionsRepository = get()
            )
        }
        factory { (token: Token.Active) ->
            HistoryPresenter(
                token = token,
                historyInteractor = get(),
                receiveAnalytics = get(),
                swapAnalytics = get(),
                analyticsInteractor = get(),
                sendAnalytics = get(),
                renBtcInteractor = get(),
                closeInteractor = get()
            )
        } bind HistoryContract.Presenter::class
        factory { (state: TransactionDetailsLaunchState) ->
            TransactionDetailsPresenter(
                state = state,
                userLocalRepository = get(),
                context = get(),
                historyInteractor = get()
            )
        } bind TransactionDetailsContract.Presenter::class
    }

    private fun Module.dataLayer() {
        factory { HistoryRemoteRepository(compareApi = get()) } bind HistoryRepository::class

        factory { ConfirmedTransactionParser }
        factory { TransactionDetailsMapper(confirmedTransactionParser = get(), gson = get()) }
        factory { HistoryTransactionMapper(userLocalRepository = get()) }
        single {
            TransactionsHistoryRepository(
                rpcRepository = get(),
                tokenKeyProvider = get(),
                transactionDaoDelegate = get(),
                transactionDetailsMapper = get(),
                historyTransactionMapper = get(),
                dispatchers = get()
            )
        }
    }
}