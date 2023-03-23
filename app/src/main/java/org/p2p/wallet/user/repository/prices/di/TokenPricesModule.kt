package org.p2p.wallet.user.repository.prices.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.api.CryptoCompareApi
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.CompareTokenInterceptor
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCoinGeckoRepository
import org.p2p.wallet.user.repository.prices.impl.TokenPricesCryptoCompareRepository

object TokenPricesModule : InjectionModule {
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
        single {
            val baseUrl = androidContext().getString(R.string.coinGeckoBaseUrl)
            val coinGeckoApi = getRetrofit(
                baseUrl = baseUrl,
                tag = "CoinGecko",
                interceptor = null
            )
                .create(CoinGeckoApi::class.java)

            TokenPricesCoinGeckoRepository(
                coinGeckoApi = coinGeckoApi,
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
