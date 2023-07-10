package org.p2p.wallet.user.repository.prices.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.wallet.R
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.core.network.NetworkCoreModule.getClient
import org.p2p.wallet.infrastructure.network.coingecko.CoinGeckoApi
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCoinGeckoRepository

object CoinGeckoTokenPricesModule : InjectionModule {

    private const val COINGECKO_TIMEOUT = 5L

    override fun create() = module {
        singleOf(::PriceCacheRepository)
        single {
            TokenPricesCoinGeckoRepository(
                coinGeckoApi = getCoinGeckoRetrofit(),
                priceCacheRepository = get(),
                dispatchers = get()
            )
        } bind TokenPricesRemoteRepository::class
    }

    private fun Scope.getCoinGeckoRetrofit(): CoinGeckoApi {
        val baseUrl = androidContext().getString(R.string.coinGeckoBaseUrl)
        val client = getClient(
            connectTimeoutSec = COINGECKO_TIMEOUT,
            readTimeoutSec = COINGECKO_TIMEOUT,
            tag = "CoinGecko",
            clientProtocols = listOf(okhttp3.Protocol.HTTP_1_1)
        )
        return getRetrofit(
            baseUrl = baseUrl,
            client = client
        )
            .create(CoinGeckoApi::class.java)
    }
}
