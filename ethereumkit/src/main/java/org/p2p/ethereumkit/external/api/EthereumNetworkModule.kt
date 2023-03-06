package org.p2p.ethereumkit.external.api

import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.ethereumkexternal.core.GsonProvider
import org.p2p.ethereumkit.external.api.alchemy.AlchemyService
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.logging.Logger

internal const val QUALIFIER_ETH_HTTP_CLIENT = "ethereum"
internal const val QUALIFIER_ETH_GSON = "ethereum_gson"

internal object EthereumNetworkModule {

    fun create(): Module = module {
        single(named(QUALIFIER_ETH_HTTP_CLIENT)) { getOkHttpClient() }
        single { getRetrofit(get(named(QUALIFIER_ETH_HTTP_CLIENT)), get(named(QUALIFIER_ETH_GSON))) }
        single(named(QUALIFIER_ETH_GSON)) { GsonProvider().provide() }
        single { get<Retrofit>().create(AlchemyService::class.java) }
        single { get<Retrofit>().create(CoinGeckoService::class.java) }
    }

    private fun getOkHttpClient(): OkHttpClient {
        val logger = Logger.getLogger("EthereumApi")
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            logger.info(message)
        }.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val headersInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.header("Authorization", Credentials.basic("", ""))
            chain.proceed(requestBuilder.build())
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headersInterceptor)
        return httpClient.build()
    }

    private fun getRetrofit(httpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(EthereumNetworkEnvironment.ALCHEMY_DEMO.baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }
}
