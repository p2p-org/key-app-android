package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.interactor.MarketInteractor
import com.p2p.wallet.swap.interactor.OpenOrdersInteractor
import com.p2p.wallet.swap.interactor.SerializationInteractor
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.interactor.SwapMarketInteractor
import com.p2p.wallet.swap.ui.SwapContract
import com.p2p.wallet.swap.ui.SwapPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object SwapModule : InjectionModule {

    override fun create() = module {

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

        factory { MarketInteractor(get()) }
        factory { OpenOrdersInteractor(get()) }
        factory { SerializationInteractor(get(), get()) }
        factory { SwapInteractor(get(), get()) }
        factory { SwapInstructionsInteractor(get()) }
        factory { SwapMarketInteractor(get()) }

        factory { (token: Token?) ->
            SwapPresenter(
                initialToken = token,
                userInteractor = get(),
                swapInteractor = get(),
                serumSwapInteractor = get()
            )
        } bind SwapContract.Presenter::class
    }
}