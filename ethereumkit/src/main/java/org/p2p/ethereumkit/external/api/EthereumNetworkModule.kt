package org.p2p.ethereumkit.external.api

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.logging.Logger
import org.p2p.core.rpc.RpcApi
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.core.GsonProvider

internal const val QUALIFIER_ETH_HTTP_CLIENT = "eth_http_client"
internal const val QUALIFIER_ETH_RETROFIT = "eth_alchemy_retrofit"
const val QUALIFIER_RPC_GSON = "ethereum_gson"

internal object EthereumNetworkModule {

    fun create(): Module = module {
        single(named(QUALIFIER_ETH_HTTP_CLIENT)) { getOkHttpClient() }
        single(named(QUALIFIER_ETH_RETROFIT)) {
            getRetrofit(
                httpClient = get(named(QUALIFIER_ETH_HTTP_CLIENT)),
                gson = get(named(QUALIFIER_RPC_GSON)))
        }
        single(named(QUALIFIER_RPC_GSON)) { GsonProvider().provide() }
        single { get<Retrofit>(named(QUALIFIER_ETH_RETROFIT)).create(RpcApi::class.java) }
        single { get<Retrofit>(named(QUALIFIER_ETH_RETROFIT)).create(CoinGeckoService::class.java) }
    }

    internal fun getOkHttpClient(): OkHttpClient {
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

    internal fun getRetrofit(httpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(EthereumNetworkEnvironment.ALCHEMY_DEMO.baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }
}
