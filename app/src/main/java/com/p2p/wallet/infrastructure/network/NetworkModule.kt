package com.p2p.wallet.infrastructure.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.environment.DataHubInterceptor
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.BigDecimalTypeAdapter
import com.p2p.wallet.rpc.RpcApi
import com.p2p.wallet.rpc.RpcRemoteRepository
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.user.UserModule.createLoggingInterceptor
import okhttp3.OkHttpClient
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.rpc.Environment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

object NetworkModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 60L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 60L

    override fun create() = module {
        single { TokenKeyProvider(get(), get(), get()) }

        single {
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .disableHtmlEscaping()
                .create()
        }

        single {
            val serum = getRetrofit(Environment.PROJECT_SERUM.endpoint, "OkHttpClient")
            val serumRpcApi = serum.create(RpcApi::class.java)

            val mainnet = getRetrofit(Environment.MAINNET.endpoint, "OkHttpClient")
            val mainnetRpcApi = mainnet.create(RpcApi::class.java)

            val datahub = getRetrofit(Environment.DATAHUB.endpoint, "OkHttpClient")
            val datahubRpcApi = datahub.create(RpcApi::class.java)

            RpcRemoteRepository(serumRpcApi, mainnetRpcApi, datahubRpcApi, get())
        } bind RpcRepository::class
    }

    private fun Scope.getRetrofit(baseUrl: String, tag: String): Retrofit {
        val client = OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor(tag)) }
            .addInterceptor(DataHubInterceptor(get()))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ResponseConverterFactory(get()))
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .client(client)
            .build()
    }
}