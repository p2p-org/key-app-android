package org.p2p.ethereumkit.external.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.ethereumkit.external.api.interceptor.EthereumApiLoggingInterceptor
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment

internal const val QUALIFIER_ETH_HTTP_CLIENT = "eth_http_client"
internal const val QUALIFIER_ETH_RETROFIT = "eth_alchemy_retrofit"

internal object EthereumNetworkModule {

    fun create(): Module = module {
        single(named(QUALIFIER_ETH_HTTP_CLIENT)) { getOkHttpClient() }
        single(named(QUALIFIER_ETH_RETROFIT)) {
            getRetrofit(
                baseUrl = EthereumNetworkEnvironment.ALCHEMY.baseUrl,
                httpClient = get(named(QUALIFIER_ETH_HTTP_CLIENT)),
                gson = get(named(RPC_JSON_QUALIFIER))
            )
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(EthereumApiLoggingInterceptor())
        return httpClient.build()
    }

    private fun getRetrofit(httpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }
}
