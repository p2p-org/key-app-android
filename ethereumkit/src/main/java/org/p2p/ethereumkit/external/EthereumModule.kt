package org.p2p.ethereumkit.external

import org.koin.core.module.Module
import org.koin.dsl.module
import org.p2p.ethereumkit.external.balance.BalanceRepository
import org.p2p.ethereumkit.external.balance.EthereumBalanceRepository
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.repository.EthereumKitRepository
import org.p2p.ethereumkit.external.repository.EthereumRepository

object EthereumModule {

    fun create(): Module = module {
        single {
            val seedPhrase = "apart approve black comfort steel spin real renew tone primary key cherry".split(" ")
            EthTokenKeyProvider(seedPhrase)
        }
        single<EthereumRepository> { EthereumKitRepository(get(), get()) }
        single<BalanceRepository> {
            EthereumBalanceRepository(
                alchemyService = get(),
                networkEnvironment = EthereumNetworkEnvironment.ALCHEMY,
                gson = get()
            )
        }
    }
}
