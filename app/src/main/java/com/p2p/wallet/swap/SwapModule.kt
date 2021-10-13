package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.interactor.SwapSerializationInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaSwapAmountInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import com.p2p.wallet.swap.repository.orca.OrcaSwapInMemoryRepository
import com.p2p.wallet.swap.repository.orca.OrcaSwapLocalRepository
import com.p2p.wallet.swap.repository.orca.OrcaSwapRemoteRepository
import com.p2p.wallet.swap.repository.orca.OrcaSwapRepository
import com.p2p.wallet.swap.ui.orca.OrcaSwapContract
import com.p2p.wallet.swap.ui.orca.OrcaSwapPresenter
import com.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import com.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapAmountInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInstructionsInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
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

        factory { SerumMarketInteractor(get()) }
        factory { SerumOpenOrdersInteractor(get()) }
        factory { SwapSerializationInteractor(get(), get()) }
        factory { SerumSwapAmountInteractor(get(), get()) }
        factory { SerumSwapInstructionsInteractor(get()) }
        factory { SerumSwapMarketInteractor(get()) }

        factory { OrcaSwapInteractor(get(), get(), get(), get(), get(), get()) }
        factory { OrcaSwapAmountInteractor(get()) }
        factory { OrcaSwapRemoteRepository(get()) } bind OrcaSwapRepository::class
        factory { OrcaSwapInMemoryRepository() } bind OrcaSwapLocalRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(token, get(), get(), get())
        } bind OrcaSwapContract.Presenter::class
    }
}