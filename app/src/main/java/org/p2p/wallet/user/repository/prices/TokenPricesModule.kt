package org.p2p.wallet.user.repository.prices

import android.content.Context
import okhttp3.OkHttpClient
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.api.CryptoCompareApi
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.infrastructure.network.NetworkModule.httpLoggingInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.CompareTokenInterceptor
import org.p2p.wallet.user.repository.prices.sources.CoinGeckoApiClient
import org.p2p.wallet.user.repository.prices.sources.CryptoCompareApiClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TokenPricesModule : InjectionModule {
    override fun create() = module {
        single {
            val client = createOkHttpClient()
                .addInterceptor(CompareTokenInterceptor())
                .apply { if (BuildConfig.DEBUG) addInterceptor(httpLoggingInterceptor("CryptoCompare")) }
                .build()

            val compareApi = Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.compareBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(CryptoCompareApi::class.java)

            CryptoCompareApiClient(compareApi)
        }
        single {
            val client = createOkHttpClient()
                .apply { if (BuildConfig.DEBUG) addInterceptor(httpLoggingInterceptor("CoinGecko")) }
                .build()

            val coinGeckoApi = Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.coinGeckoBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(CoinGeckoApi::class.java)

            CoinGeckoApiClient(coinGeckoApi, get(), get())
        }

        single<TokenPricesRepository> {
            TokenPricesRemoteRepository(
                cryptoCompareApiClient = get(),
                coinGeckoClient = get(),
                appFeatureFlags = get(),
                dispatchers = get()
            )
        }
    }

    private fun createOkHttpClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(NetworkModule.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(NetworkModule.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}
