package org.p2p.wallet.swap

import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.swap.interactor.SwapSerializationInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapAmountInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.repository.OrcaSwapInMemoryRepository
import org.p2p.wallet.swap.repository.OrcaSwapLocalRepository
import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.swap.ui.orca.OrcaSwapContract
import org.p2p.wallet.swap.ui.orca.OrcaSwapPresenter
import org.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import org.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapAmountInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapInstructionsInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
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