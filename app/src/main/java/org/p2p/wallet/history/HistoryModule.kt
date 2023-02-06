package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.api.HistoryServiceApi
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.HistoryServiceInteractor
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionConverter
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepositoryImpl
import org.p2p.wallet.history.repository.remote.RpcHistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRpcRepository
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsContract
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.history.ui.new_history.NewHistoryContract
import org.p2p.wallet.history.ui.new_history.NewHistoryPresenter
import org.p2p.wallet.history.ui.token.TokenHistoryContract
import org.p2p.wallet.history.ui.token.TokenHistoryPresenter
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.sell.interactor.HistoryItemMapper
import retrofit2.Retrofit

object HistoryModule : InjectionModule {

    override fun create(): Module = module {
        dataLayer()

        factoryOf(::HistoryTransactionConverter)
        factoryOf(::HistoryTransactionMapper)
        factory {
            HistoryInteractor(
                rpcAccountRepository = get(),
                transactionsLocalRepository = get(),
                transactionsRemoteRepository = get(),
                tokenKeyProvider = get(),
                historyTransactionMapper = get(),
                rpcSignatureRepository = get(),
                userInteractor = get(),
                sellInteractor = get(),
                hiddenSellTransactionsStorage = get(),
                sellEnabledFeatureToggle = get(),
                serviceScope = get()
            )
        }
        factoryOf(::HistoryItemMapper)
        factoryOf(::HistorySellTransactionMapper)

        factoryOf(::HistoryPresenter) bind HistoryContract.Presenter::class
        factoryOf(::TokenHistoryPresenter) bind TokenHistoryContract.Presenter::class
        factoryOf(::TransactionDetailsPresenter) bind TransactionDetailsContract.Presenter::class
        factoryOf(::HistoryTransactionDetailsBottomSheetPresenter) {
            bind<HistoryTransactionDetailsContract.Presenter>()
        }
    }

    private fun Module.dataLayer() {
        factoryOf(::TransactionDetailsEntityMapper)
        singleOf(::TransactionDetailsDatabaseRepository) bind TransactionDetailsLocalRepository::class
        single { get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER)).create(RpcHistoryApi::class.java) }
        singleOf(::TransactionDetailsRpcRepository) bind TransactionDetailsRemoteRepository::class

        factory { RpcHistoryRemoteRepository(get(), get(), get()) }
        factory<NewHistoryContract.Presenter> { NewHistoryPresenter(get()) }


        factory { HistoryServiceSignatureFieldGenerator(get()) }

        single { get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER)).create(HistoryServiceApi::class.java) }

        factory { HistoryServiceInteractor(get()) }

        factory<HistoryRemoteRepository> { HistoryRemoteRepositoryImpl(get()) }
        factory<List<HistoryRemoteRepository>> {
            listOf(
                RpcHistoryRemoteRepository(
                    historyApi = get(),
                    tokenKeyProvider = get(),
                    historyServiceSignatureFieldGenerator = get()
                )
            )
        }
    }
}
