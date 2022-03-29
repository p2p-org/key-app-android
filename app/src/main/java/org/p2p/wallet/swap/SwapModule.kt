package org.p2p.wallet.swap

import android.content.Context
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.api.InternalWebApi
import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import org.p2p.wallet.swap.interactor.SwapSerializationInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInstructionsInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaNativeSwapInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaRouteInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import org.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapAmountInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
import org.p2p.wallet.swap.repository.OrcaSwapInternalRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapInternalRepository
import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.swap.ui.orca.OrcaSwapContract
import org.p2p.wallet.swap.ui.orca.OrcaSwapPresenter

object SwapModule : InjectionModule {

    override fun create() = module {
        single {
            val baseUrl = get<Context>().getString(R.string.p2pWebBaseUrl)
            getRetrofit(baseUrl, "p2pWeb", null).create(InternalWebApi::class.java)
        }

        single {
            OrcaSwapInternalRemoteRepository(get(), get())
        } bind OrcaSwapInternalRepository::class

        single {
            SerumSwapInteractor(
                instructionsInteractor = get(),
                openOrdersInteractor = get(),
                marketInteractor = get(),
                swapMarketInteractor = get(),
                transactionInteractor = get(),
                tokenKeyProvider = get()
            )
        }

        factory { SerumMarketInteractor(get()) }
        factory { SerumOpenOrdersInteractor(get(), get()) }
        factory { SwapSerializationInteractor(get()) }
        factory { SerumSwapAmountInteractor(get()) }
        factory { SwapInstructionsInteractor(get(), get()) }
        factory { SerumSwapMarketInteractor(get()) }

        single {
            OrcaSwapInteractor(
                feeRelayerSwapInteractor = get(),
                feeRelayerAccountInteractor = get(),
                feeRelayerInteractor = get(),
                orcaRouteInteractor = get(),
                orcaInfoInteractor = get(),
                orcaPoolInteractor = get(),
                rpcAmountRepository = get(),
                orcaNativeSwapInteractor = get(),
                environmentManager = get(),
                tokenKeyProvider = get()
            )
        }
        single { OrcaInfoInteractor(get()) }
        single { OrcaRouteInteractor(get(), get()) }
        factory { OrcaInstructionsInteractor(get()) }
        factory { OrcaPoolInteractor(get(), get(), get(), get()) }
        factory { OrcaNativeSwapInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

        factory { TransactionAddressInteractor(get(), get(), get()) }

        factory { OrcaSwapRemoteRepository(get(), get(), get()) } bind OrcaSwapRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(
                initialToken = token,
                appScope = get(),
                userInteractor = get(),
                swapInteractor = get(),
                orcaPoolInteractor = get(),
                settingsInteractor = get(),
                browseAnalytics = get(),
                analyticsInteractor = get(),
                swapAnalytics = get(),
                transactionManager = get(),
            )
        } bind OrcaSwapContract.Presenter::class
    }
}
