package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.buffer.TokensHistoryBuffer
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionConverter
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.TransactionDetailsRpcRepository
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.TransactionDetailsBottomSheetContract
import org.p2p.wallet.history.ui.detailsbottomsheet.TransactionDetailsBottomSheetPresenter
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.token.TokenHistoryContract
import org.p2p.wallet.history.ui.token.TokenHistoryPresenter
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
                rpcAccountRepository = get(),
                transactionsLocalRepository = get(),
                tokenKeyProvider = get(),
                historyTransactionMapper = get(),
                userInteractor = get(),
                tokensHistoryBuffer = get()
            )
        }
        single {
            TokensHistoryBuffer(
                rpcSignatureRepository = get(),
                transactionRepository = get(),
                serviceScope = get(),
                tokenKeyProvider = get()
            )
        }
        factory { (token: Token.Active) ->
            TokenHistoryPresenter(
                token = token,
                historyInteractor = get(),
                receiveAnalytics = get(),
                swapAnalytics = get(),
                analyticsInteractor = get(),
                sendAnalytics = get(),
                renBtcInteractor = get(),
                tokenInteractor = get()
            )
        } bind TokenHistoryContract.Presenter::class
        factory { (state: TransactionDetailsLaunchState) ->
            TransactionDetailsPresenter(
                resources = get(),
                theme = get(),
                state = state,
                userLocalRepository = get(),
                historyInteractor = get()
            )
        } bind TransactionDetailsContract.Presenter::class
        factory { (state: TransactionDetailsLaunchState) ->
            TransactionDetailsBottomSheetPresenter(
                state = state,
                historyInteractor = get()
            )
        } bind TransactionDetailsBottomSheetContract.Presenter::class
    }

    private fun Module.dataLayer() {
        factory { TransactionDetailsEntityMapper(get()) }
        single {
            TransactionDetailsDatabaseRepository(
                daoDelegate = get(),
                mapper = get()
            )
        } bind TransactionDetailsLocalRepository::class

        single {
            val api = get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER))
                .create(RpcHistoryApi::class.java)

            TransactionDetailsRpcRepository(
                rpcApi = api,
                userInteractor = get()
            )
        } bind TransactionDetailsRemoteRepository::class

        factory {
            HistoryPresenter(
                historyInteractor = get(),
                renBtcInteractor = get(),
                receiveAnalytics = get(),
                swapAnalytics = get(),
                analyticsInteractor = get(),
                environmentManager = get(),
                sendAnalytics = get()
            )
        } bind HistoryContract.Presenter::class
    }
}
