package org.p2p.ethereumkit.external

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.ethereumkit.external.api.QUALIFIER_ETH_GSON
import org.p2p.ethereumkit.external.balance.EthereumTokensRemoteRepository
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.price.EthereumPriceRepository
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.external.repository.EthereumKitRepository
import org.p2p.ethereumkit.external.repository.EthereumRepository

object EthereumModule {

    const val BRIDGES_SERVICE_RETROFIT_QUALIFIER = "BRIDGES_SERVICE_RETROFIT_QUALIFIER"

    fun create(): Module = module {
        singleOf(::EthereumKitRepository) bind EthereumRepository::class
        singleOf(::EthereumPriceRepository) bind PriceRepository::class
        singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class
        single<EthereumTokensRepository> {
            EthereumTokensRemoteRepository(
                alchemyService = get(),
                networkEnvironment = EthereumNetworkEnvironment.ALCHEMY,
                gson = get(named(QUALIFIER_ETH_GSON))
            )
        }
    }
}
