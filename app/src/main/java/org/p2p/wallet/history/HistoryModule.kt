package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionConverter
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRpcRepository
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsContract
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetPresenter
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
                transactionsRemoteRepository = get(),
                rpcSignatureRepository = get(),
                serviceScope = get()
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
                resourcesProvider = get(),
                state = state,
                userLocalRepository = get(),
                historyInteractor = get()
            )
        } bind TransactionDetailsContract.Presenter::class
        factory { (state: TransactionDetailsLaunchState) ->
            HistoryTransactionDetailsBottomSheetPresenter(
                state = state,
                historyInteractor = get(),
                usernameInteractor = get()
            )
        } bind HistoryTransactionDetailsContract.Presenter::class
    }

    private fun Module.dataLayer() {
        factory { TransactionDetailsEntityMapper(get()) }
        singleOf(::TransactionDetailsDatabaseRepository) bind TransactionDetailsLocalRepository::class

        single {
            val api = get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER))
                .create(RpcHistoryApi::class.java)

            TransactionDetailsRpcRepository(
                rpcApi = api,
                transactionParsingContext = get()
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
