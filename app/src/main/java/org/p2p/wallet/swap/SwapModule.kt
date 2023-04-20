package org.p2p.wallet.swap

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.api.OrcaApi
import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaRouteInteractor
import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository

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
        factory { SwapInstructionsInteractor(get(), get()) }

        single { OrcaInfoInteractor(get(), get()) }
        single { OrcaRouteInteractor(get(), get()) }
        factory { OrcaPoolInteractor(get(), get(), get(), get()) }

        factory { TransactionAddressInteractor(get(), get(), get()) }

        single { OrcaSwapRemoteRepository(get(), get(), get(), get()) } bind OrcaSwapRepository::class
    }
}
