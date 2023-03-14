package org.p2p.ethereumkit.external.api

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.ethereumkit.external.core.GsonProvider
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.logging.Logger
import org.p2p.core.rpc.RpcApi

internal const val QUALIFIER_ETH_HTTP_CLIENT = "ethereum"
const val QUALIFIER_RPC_GSON = "ethereum_gson"

internal object EthereumNetworkModule {

    fun create(): Module = module {
        single(named(QUALIFIER_ETH_HTTP_CLIENT)) { getOkHttpClient() }
        single { getRetrofit(get(named(QUALIFIER_ETH_HTTP_CLIENT)), get(named(QUALIFIER_RPC_GSON))) }
        single(named(QUALIFIER_RPC_GSON)) { GsonProvider().provide() }
        single { get<Retrofit>().create(RpcApi::class.java) }
        single { get<Retrofit>().create(CoinGeckoService::class.java) }
    }

    private fun getOkHttpClient(): OkHttpClient {
        val logger = Logger.getLogger("EthereumApi")
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            logger.info(message)
        }.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val headersInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
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
