package org.p2p.ethereumkit.external

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.ethereumkit.external.balance.TokensRepository
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.price.EthereumPriceRepository
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.external.repository.EthereumKitRepository
import org.p2p.ethereumkit.external.repository.EthereumRepository

internal object EthereumModule {

    fun create(): Module = module {

        single<EthereumRepository> { EthereumKitRepository(get(), get(), get()) }

        single<TokensRepository> {
            EthereumTokensRepository(
                alchemyService = get(),
                networkEnvironment = EthereumNetworkEnvironment.ALCHEMY,
                gson = get()
            )
        }
        single<PriceRepository> {
            EthereumPriceRepository(priceApi = get())
        }

        singleOf<CoroutineDispatchers> { DefaultDispatchers() }
    }
}
