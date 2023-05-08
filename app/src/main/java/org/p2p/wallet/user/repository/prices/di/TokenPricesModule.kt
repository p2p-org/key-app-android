package org.p2p.wallet.user.repository.prices.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import timber.log.Timber
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.api.CryptoCompareApi
import org.p2p.wallet.infrastructure.network.NetworkModule.getClient
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.CompareTokenInterceptor
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCoinGeckoRepository
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCryptoCompareRepository

object TokenPricesModule : InjectionModule {

    private const val COINGECKO_TIMEOUT = 5L

    override fun create() = module {
        single {
            val baseUrl = androidContext().getString(R.string.compareBaseUrl)
            val compareApi = getRetrofit(
                baseUrl = baseUrl,
                tag = "CryptoCompare",
                interceptor = CompareTokenInterceptor()
            )
                .create(CryptoCompareApi::class.java)

            TokenPricesCryptoCompareRepository(cryptoCompareApi = compareApi, dispatchers = get())
        }
        singleOf(::PriceCacheRepository)
        single {
            val baseUrl = androidContext().getString(R.string.coinGeckoBaseUrl)
            val client = getClient(
                connectTimeoutSec = COINGECKO_TIMEOUT,
                readTimeoutSec = COINGECKO_TIMEOUT,
                tag = "CoinGecko",
                clientProtocols = listOf(okhttp3.Protocol.HTTP_1_1)
            )
            val coinGeckoApi = getRetrofit(
                baseUrl = baseUrl,
                client = client
            )
                .create(CoinGeckoApi::class.java)

            TokenPricesCoinGeckoRepository(
                coinGeckoApi = coinGeckoApi,
                priceCacheRepository = get(),
                dispatchers = get()
            )
        }

        single {
            val shouldInjectCoinGeckoApi = get<InAppFeatureFlags>().useCoinGeckoForPrices.featureValue
            Timber.i("Injecting TokenPricesRemoteRepository, useCoinGeckoForPrices=$shouldInjectCoinGeckoApi")
            if (shouldInjectCoinGeckoApi) {
                get<TokenPricesCoinGeckoRepository>()
            } else {
                get<TokenPricesCryptoCompareRepository>()
            }
        }
    }
}
