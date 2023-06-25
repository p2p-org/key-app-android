package org.p2p.market.price

import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.market.price.repository.MarketPriceLocalRepository
import org.p2p.market.price.repository.MarketPriceRemoteRepository
import org.p2p.market.price.repository.MarketPriceRepository

object MarketPriceModule: InjectionModule {

    override fun create() = module {
        single<MarketPriceRepository> {
            MarketPriceRemoteRepository(
                api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(),
                gson = get(named(RPC_JSON_QUALIFIER)),
                urlProvider = get()
            )
        }

        single { MarketPriceLocalRepository() }
    }
}
