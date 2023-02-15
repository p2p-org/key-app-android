package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.databinding.ActivityRootBinding.bind
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.interactor.HistoryServiceInteractor
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepositoryFacade
import org.p2p.wallet.history.repository.remote.MoonpayHistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.RpcHistoryRemoteRepository
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.history.ui.details.TransactionDetailsContract
import org.p2p.wallet.history.ui.details.TransactionDetailsPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsContract
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.history.ui.historylist.HistoryListViewPresenter
import org.p2p.wallet.history.ui.token.TokenHistoryContract
import org.p2p.wallet.history.ui.token.TokenHistoryPresenter
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.sell.interactor.HistoryItemMapper
import retrofit2.create

object HistoryModule : InjectionModule {

    override fun create(): Module = module {
        dataLayer()

        factoryOf(::RpcHistoryTransactionConverter)
        factoryOf(::HistoryItemMapper)
        factoryOf(::HistorySellTransactionMapper)

        factoryOf(::HistoryPresenter) bind HistoryContract.Presenter::class
        factoryOf(::TokenHistoryPresenter) bind TokenHistoryContract.Presenter::class
        factory { (token: Token.Active?) ->
            HistoryListViewPresenter(
                token = token,
                historyInteractor = get(),
                hiddenSellTransactionsStorage = get(),
                environmentManager = get(),
                sellTransactionsMapper = get(),
                historyItemMapper = get()
            )
        } bind HistoryListViewContract.Presenter::class
        factoryOf(::TransactionDetailsPresenter) bind TransactionDetailsContract.Presenter::class
        factoryOf(::HistoryTransactionDetailsBottomSheetPresenter) bind
            HistoryTransactionDetailsContract.Presenter::class
    }

    private fun Module.dataLayer() {
        single { get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER)).create(RpcHistoryServiceApi::class.java) }
        factoryOf(::TransactionDetailsEntityMapper)
        single { HistoryServiceSignatureFieldGenerator(get()) }
        singleOf(::TransactionDetailsDatabaseRepository) bind TransactionDetailsLocalRepository::class
        single { get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER)).create(RpcHistoryApi::class.java) }
        factoryOf(::HistoryServiceInteractor)
        single<HistoryRemoteRepository> {
            val remotes = listOf(
                RpcHistoryRemoteRepository(
                    get(),
                    get(),
                    get(),
                    get()
                ),
                MoonpayHistoryRemoteRepository(
                    get(), get(), get()
                )
            )
            HistoryRemoteRepositoryFacade(remotes)
        }
    }
}
