package com.p2p.wallet.infrastructure.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.BigDecimalTypeAdapter
import com.p2p.wallet.rpc.api.FeeRelayerApi
import com.p2p.wallet.rpc.api.RpcApi
import com.p2p.wallet.rpc.repository.FeeRelayerRemoteRepository
import com.p2p.wallet.rpc.repository.FeeRelayerRepository
import com.p2p.wallet.rpc.repository.RpcRemoteRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.UserModule.createLoggingInterceptor
import okhttp3.Interceptor
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
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }

        single {
            val serum = getRetrofit(Environment.SOLANA.endpoint)
            val serumRpcApi = serum.create(RpcApi::class.java)

            val mainnet = getRetrofit(Environment.MAINNET.endpoint)
            val mainnetRpcApi = mainnet.create(RpcApi::class.java)

            RpcRemoteRepository(serumRpcApi, mainnetRpcApi, get())
        } bind RpcRepository::class

        single {
//            val errorInterceptor
            val baseUrl = get<Context>().getString(R.string.feeRelayerBaseUrl)
            val retrofit =  Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
                .client(getClient("FeeRelayer"))
                .build()

            val api = retrofit.create(FeeRelayerApi::class.java)
            FeeRelayerRemoteRepository(api)
        } bind FeeRelayerRepository::class
    }

    private fun Scope.getRetrofit(
        baseUrl: String,
        tag: String = "OkHttpClient"
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .client(getClient(tag))
            .build()
    }

    private fun Scope.getClient(tag: String, interceptor: Interceptor? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor(tag))
                if (interceptor != null) addInterceptor(interceptor)
            }
            .addNetworkInterceptor(ContentTypeInterceptor())
            .build()
    }
}