package org.p2p.ethereumkit.external

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.external.api.QUALIFIER_ETH_RETROFIT
import org.p2p.ethereumkit.external.token.EthereumTokenRemoteRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.repository.EthereumKitRepository
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.external.token.EthereumTokenInMemoryRepository
import org.p2p.ethereumkit.external.token.EthereumTokenLocalRepository
import org.p2p.ethereumkit.external.token.EthereumTokenRepository

object EthereumModule {

    fun create(): Module = module {
        singleOf(::EthereumKitRepository) bind EthereumRepository::class
        singleOf(::DefaultDispatchers) bind CoroutineDispatchers::class
        single<EthereumTokenRepository> {
            EthereumTokenRemoteRepository(
                alchemyService = get<Retrofit>(named(QUALIFIER_ETH_RETROFIT)).create(),
                networkEnvironment = EthereumNetworkEnvironment.ALCHEMY,
                gson = get(named(RPC_JSON_QUALIFIER))
            )
        }
        singleOf(::EthereumTokenInMemoryRepository) bind EthereumTokenLocalRepository::class

        includes(EthereumNetworkModule.create())
    }
}
