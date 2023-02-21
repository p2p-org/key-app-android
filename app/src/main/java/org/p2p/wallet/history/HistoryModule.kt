package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.HistoryRepository
import org.p2p.wallet.history.repository.remote.MoonpayHistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.RpcHistoryRepository
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetPresenter
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsContract
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.history.ui.historylist.HistoryListViewPresenter
import org.p2p.wallet.history.ui.token.TokenHistoryContract
import org.p2p.wallet.history.ui.token.TokenHistoryPresenter
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.sell.interactor.HistoryItemMapper

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
                historyInteractor = get(),
                environmentManager = get(),
                historyItemMapper = get(),
                token = token
            )
        } bind HistoryListViewContract.Presenter::class
        factoryOf(::HistoryTransactionDetailsBottomSheetPresenter) bind
            HistoryTransactionDetailsContract.Presenter::class
    }

    private fun Module.dataLayer() {
        single {
            get<Retrofit>(named(PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER)).create(
                RpcHistoryServiceApi::class.java
            )
        }
        factoryOf(::TransactionDetailsEntityMapper)
        singleOf(::HistoryServiceSignatureFieldGenerator)
        singleOf(::TransactionDetailsDatabaseRepository) bind TransactionDetailsLocalRepository::class
        single { get<Retrofit>(named(RpcModule.RPC_RETROFIT_QUALIFIER)).create(RpcHistoryApi::class.java) }
        factoryOf(::HistoryInteractor)
        single<HistoryRemoteRepository> {
            val remotes = listOf(
                RpcHistoryRepository(
                    historyApi = get(),
                    tokenKeyProvider = get(),
                    historyServiceSignatureFieldGenerator = get(),
                    converter = get()
                ),
                MoonpayHistoryRemoteRepository(
                    sellEnabledFeatureToggle = get(),
                    repository = get(),
                    tokenKeyProvider = get()
                )
            )
            HistoryRepository(remotes)
        }
    }
}
