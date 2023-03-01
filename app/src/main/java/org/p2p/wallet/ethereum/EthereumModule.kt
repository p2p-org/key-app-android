package org.p2p.wallet.ethereum

import org.koin.core.module.Module
import org.koin.dsl.module
import org.p2p.ethereumkit.repository.EthereumKitRepository
import org.p2p.ethereumkit.repository.EthereumRepository
import org.p2p.wallet.common.di.InjectionModule

object EthereumModule : InjectionModule {

    override fun create(): Module = module {
        single<EthereumRepository> { EthereumKitRepository() }
    }
}
