package org.p2p.wallet.infrastructure.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayErrorInterceptor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.home.HomeModule.MOONPAY_QUALIFIER
import org.p2p.wallet.home.model.BigDecimalTypeAdapter
import org.p2p.wallet.infrastructure.network.interceptor.RpcInterceptor
import org.p2p.wallet.rpc.RpcModule.RPC_QUALIFIER
import org.p2p.wallet.updates.ConnectionStateProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

object NetworkModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 60L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 60L

    override fun create() = module {
        single { EnvironmentManager(get(), get()) }
        single { TokenKeyProvider(get()) }

        single {
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }

        single { ConnectionStateProvider(get()) }

        single(named(MOONPAY_QUALIFIER)) {
            val moonPayApiUrl = get<Context>().getString(R.string.moonpayBaseUrl)
            getRetrofit(moonPayApiUrl, "Moonpay", MoonpayErrorInterceptor(get()))
        }
        single(named(RPC_QUALIFIER)) {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(rpcApiUrl, "Rpc", RpcInterceptor(get(), get()))
        }
    }

    fun Scope.getRetrofit(
        baseUrl: String,
        tag: String = "OkHttpClient",
        interceptor: Interceptor?
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .client(getClient(tag, interceptor))
            .build()
    }

    fun Scope.createLoggingInterceptor(tag: String): HttpLoggingInterceptor {
        val gson = get<Gson>()

        val okHttpLogger = object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (!message.startsWith('{') && !message.startsWith('[')) {
                    Timber.tag(tag).d(message)
                    return
                }

                try {
                    val json = JsonParser().parse(message)
                    Timber.tag(tag).d(gson.toJson(json))
                } catch (e: Throwable) {
                    Timber.tag(tag).d(message)
                }
            }
        }
        return HttpLoggingInterceptor(okHttpLogger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
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