package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.interactor.MarketInteractor
import com.p2p.wallet.swap.interactor.OpenOrdersInteractor
import com.p2p.wallet.swap.interactor.SerializationInteractor
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.interactor.SwapInteractor2
import com.p2p.wallet.swap.interactor.SwapMarketInteractor
import com.p2p.wallet.swap.repository.SwapInMemoryRepository
import com.p2p.wallet.swap.repository.SwapLocalRepository
import com.p2p.wallet.swap.repository.SwapRemoteRepository
import com.p2p.wallet.swap.repository.SwapRepository
import com.p2p.wallet.swap.ui.SerumSwapPresenter
import com.p2p.wallet.swap.ui.SwapContract
import org.koin.dsl.bind
import org.koin.dsl.module

object SwapModule : InjectionModule {

    override fun create() = module {
        factory { SwapRemoteRepository(get()) } bind SwapRepository::class

        single { SwapInMemoryRepository() } bind SwapLocalRepository::class

        factory { SwapInteractor(get(), get(), get(), get(), get()) }

        single {
            SerumSwapInteractor(
                swapInteractor2 = get(),
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
        factory { SwapInteractor2(get()) }
        factory { SwapMarketInteractor(get()) }

        factory { (token: Token?) -> SerumSwapPresenter(token, get(), get(), get()) } bind SwapContract.Presenter::class
    }
}