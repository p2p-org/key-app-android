package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.repository.SwapInMemoryRepository
import com.p2p.wallet.swap.repository.SwapLocalRepository
import com.p2p.wallet.swap.repository.SwapRemoteRepository
import com.p2p.wallet.swap.repository.SwapRepository
import org.koin.dsl.bind
import org.koin.dsl.module

object SwapModule : InjectionModule {

    override fun create() = module {
        factory { SwapRemoteRepository(get()) } bind SwapRepository::class

        single { SwapInMemoryRepository() } bind SwapLocalRepository::class

        factory { SwapInteractor(get(), get(), get(), get(), get()) }
    }
}