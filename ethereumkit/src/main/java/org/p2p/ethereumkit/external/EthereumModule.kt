package org.p2p.ethereumkit.external

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.ethereumkit.external.api.QUALIFIER_ETH_RETROFIT
import org.p2p.ethereumkit.external.api.QUALIFIER_RPC_GSON
import org.p2p.ethereumkit.external.balance.EthereumTokensRemoteRepository
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.price.EthereumPriceRepository
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.external.repository.EthereumKitRepository
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.external.repository.EthereumTokensProvider

object EthereumModule {

    fun create(): Module = module {
        singleOf(::EthereumKitRepository) bind EthereumRepository::class
        singleOf(::EthereumPriceRepository) bind PriceRepository::class
        singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class
        single<EthereumTokensRepository> {
            EthereumTokensRemoteRepository(
                alchemyService = get<Retrofit>(named(QUALIFIER_ETH_RETROFIT)).create(),
                networkEnvironment = EthereumNetworkEnvironment.ALCHEMY,
                gson = get(named(QUALIFIER_RPC_GSON))
            )
        }
        single { EthereumTokensProvider(get(),get(),get()) }
    }
}
