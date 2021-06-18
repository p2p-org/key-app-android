package com.p2p.wallet.swap

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.repository.SwapInMemoryRepository
import com.p2p.wallet.swap.repository.SwapLocalRepository
import com.p2p.wallet.swap.repository.SwapRemoteRepository
import com.p2p.wallet.swap.repository.SwapRepository
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.data.RpcRepository

object SwapModule : InjectionModule {

    override fun create() = module {
        factory {
            val qualifier = get<EnvironmentManager>().getCurrentQualifier()
            val repository = get<RpcRepository>(named(qualifier))
            SwapRemoteRepository(repository)
        } bind SwapRepository::class

        single { SwapInMemoryRepository() } bind SwapLocalRepository::class

        factory { SwapInteractor(get(), get(), get(), get()) }
    }
}