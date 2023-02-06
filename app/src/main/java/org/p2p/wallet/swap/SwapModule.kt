package org.p2p.wallet.swap

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.api.OrcaApi
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
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.api.SwapJupiterTokensApi
import org.p2p.wallet.swap.jupiter.domain.JupiterSwapInteractor
import org.p2p.wallet.swap.jupiter.repository.JupiterRemoteMapper
import org.p2p.wallet.swap.jupiter.repository.JupiterRemoteRepository
import org.p2p.wallet.swap.jupiter.repository.JupiterSwapRoutesMapper
import org.p2p.wallet.swap.jupiter.repository.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.JupiterSwapTransactionMapper
import org.p2p.wallet.swap.jupiter.repository.JupiterSwapTransactionRepository
import org.p2p.wallet.swap.jupiter.repository.JupiterTokensRepository
import org.p2p.wallet.swap.jupiter.repository.SwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.SwapTransactionRepository
import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.swap.ui.orca.OrcaSwapContract
import org.p2p.wallet.swap.ui.orca.OrcaSwapPresenter

object SwapModule : InjectionModule {

    override fun create() = module {
        single {
            val baseUrl = androidContext().getString(R.string.orcaApiBaseUrl)
            getRetrofit(
                baseUrl = baseUrl,
                tag = "Orca",
                interceptor = null
            ).create(OrcaApi::class.java)
        }

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
                feeRelayerTopUpInteractor = get(),
                orcaInfoInteractor = get(),
                orcaNativeSwapInteractor = get(),
                environmentManager = get()
            )
        }
        single { OrcaInfoInteractor(get(), get()) }
        single { OrcaRouteInteractor(get(), get()) }
        factory { OrcaInstructionsInteractor(get()) }
        factory { OrcaPoolInteractor(get(), get(), get(), get()) }
        factory { OrcaNativeSwapInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

        factory { TransactionAddressInteractor(get(), get(), get()) }

        single { OrcaSwapRemoteRepository(get(), get(), get(), get()) } bind OrcaSwapRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(
                resources = get(),
                initialToken = token,
                appScope = get(),
                userInteractor = get(),
                swapInteractor = get(),
                orcaPoolInteractor = get(),
                settingsInteractor = get(),
                browseAnalytics = get(),
                analyticsInteractor = get(),
                swapAnalytics = get(),
                transactionBuilderInteractor = get(),
                transactionManager = get(),
            )
        } bind OrcaSwapContract.Presenter::class

        single {
            val baseUrl = androidContext().getString(R.string.jupiterCacheBaseUrl)
            getRetrofit(
                baseUrl = baseUrl,
                tag = "JupiterCache",
                interceptor = null
            ).create(SwapJupiterTokensApi::class.java)
        }

        single {
            val baseUrl = androidContext().getString(R.string.jupiterQuoteBaseUrl)
            getRetrofit(
                baseUrl = baseUrl,
                tag = "JupiterQuote",
                interceptor = null
            ).create(SwapJupiterApi::class.java)
        }
        factoryOf(::JupiterSwapRoutesMapper)
        factoryOf(::JupiterRemoteMapper)
        factoryOf(::JupiterSwapTransactionMapper)

        factory { JupiterSwapRoutesRepository(get(), get(), get()) } bind SwapRoutesRepository::class
        factory { JupiterSwapTransactionRepository(get(), get(), get()) } bind SwapTransactionRepository::class
        factory { JupiterRemoteRepository(get(), get()) } bind JupiterTokensRepository::class
        factory { JupiterSwapInteractor(get(), get(), get(), get(), get()) }
    }
}
