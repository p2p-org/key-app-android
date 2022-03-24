package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.kits.transaction.mapper.TransactionDetailsNetworkMapper
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionConverter
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.repository.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.TransactionDetailsRpcRepository
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.rpc.api.RpcHistoryApi
import retrofit2.Retrofit

object HistoryModule : InjectionModule {

    override fun create(): Module = module {
        dataLayer()

        factory {
            HistoryTransactionMapper(
                userLocalRepository = get(),
                historyTransactionConverter = HistoryTransactionConverter(),
                dispatchers = get()
            )
        }
        factory {
            HistoryInteractor(
                rpcSignatureRepository = get(),
                rpcAccountRepository = get(),
                transactionsRemoteRepository = get(),
                transactionsLocalRepository = get(),
                tokenKeyProvider = get(),
                historyTransactionMapper = get()
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
                tokenInteractor = get()
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
        factory { TransactionDetailsEntityMapper(get()) }
        single {
            TransactionDetailsDatabaseRepository(
                daoDelegate = get(),
                mapper = get()
            )
        } bind TransactionDetailsLocalRepository::class

        factory { TransactionDetailsNetworkMapper() }
        single {
            val api = get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER))
                .create(RpcHistoryApi::class.java)

            TransactionDetailsRpcRepository(
                rpcApi = api,
                dispatchers = get(),
                transactionDetailsNetworkMapper = get()
            )
        } bind TransactionDetailsRemoteRepository::class
    }
}
