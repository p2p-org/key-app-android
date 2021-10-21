package com.p2p.wallet.swap

import android.content.Context
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.api.InternalWebApi
import com.p2p.wallet.swap.interactor.SwapSerializationInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaAddressInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaAmountInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaInstructionsInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor2
import com.p2p.wallet.swap.interactor.orca.OrcaSwapPoolInteractor
import com.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import com.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapAmountInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInstructionsInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
import com.p2p.wallet.swap.repository.OrcaSwapInternalRemoteRepository
import com.p2p.wallet.swap.repository.OrcaSwapInternalRepository
import com.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import com.p2p.wallet.swap.repository.OrcaSwapRepository
import com.p2p.wallet.swap.ui.orca.OrcaSwapContract
import com.p2p.wallet.swap.ui.orca.OrcaSwapPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

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
                serializationInteractor = get(),
                tokenKeyProvider = get()
            )
        }

        factory { SerumMarketInteractor(get()) }
        factory { SerumOpenOrdersInteractor(get()) }
        factory { SwapSerializationInteractor(get(), get()) }
        factory { SerumSwapAmountInteractor(get(), get()) }
        factory { SerumSwapInstructionsInteractor(get()) }
        factory { SerumSwapMarketInteractor(get()) }

        single { OrcaSwapPoolInteractor(get()) }
        factory { OrcaInstructionsInteractor(get(), get(), get()) }
        factory { OrcaAddressInteractor(get(), get()) }
        factory { OrcaSwapInteractor2(get(), get(), get(), get(), get(), get(), get()) }
        factory { OrcaSwapInteractor(get(), get(), get(), get(), get()) }
        factory { OrcaAmountInteractor(get()) }
        factory { OrcaSwapRemoteRepository(get(), get()) } bind OrcaSwapRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(token, get(), get(), get(), get())
        } bind OrcaSwapContract.Presenter::class
    }
}