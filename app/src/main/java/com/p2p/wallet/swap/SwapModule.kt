package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.orca.interactor.OrcaPoolDataInteractor
import com.p2p.wallet.swap.orca.interactor.OrcaSwapInteractor
import com.p2p.wallet.swap.orca.repository.OrcaSwapInMemoryRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapLocalRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapRemoteRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapRepository
import com.p2p.wallet.swap.orca.ui.OrcaSwapContract
import com.p2p.wallet.swap.orca.ui.OrcaSwapPresenter
import com.p2p.wallet.swap.serum.interactor.SerumMarketInteractor
import com.p2p.wallet.swap.serum.interactor.SerumOpenOrdersInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapAmountInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapInstructionsInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapMarketInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapSerializationInteractor
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
        factory { SerumSwapSerializationInteractor(get(), get()) }
        factory { SerumSwapAmountInteractor(get(), get()) }
        factory { SerumSwapInstructionsInteractor(get()) }
        factory { SerumSwapMarketInteractor(get()) }

        factory { OrcaSwapInteractor(get(), get(), get(), get(), get()) }
        factory { OrcaPoolDataInteractor(get()) }
        factory { OrcaSwapRemoteRepository(get()) } bind OrcaSwapRepository::class
        factory { OrcaSwapInMemoryRepository() } bind OrcaSwapLocalRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(token, get(), get(), get())
        } bind OrcaSwapContract.Presenter::class
    }
}