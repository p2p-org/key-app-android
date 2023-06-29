package org.p2p.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import org.p2p.core.BuildConfig
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.crashlytics.helpers.CrashHttpLoggingInterceptor
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.data.transactionerrors.RpcTransactionErrorTypeAdapter
import org.p2p.core.network.data.transactionerrors.RpcTransactionInstructionErrorParser
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.network.gson.Base58TypeAdapter
import org.p2p.core.network.gson.Base64TypeAdapter
import org.p2p.core.network.gson.BigDecimalTypeAdapter
import org.p2p.core.network.gson.GsonProvider
import org.p2p.core.network.interceptor.ContentTypeInterceptor
import org.p2p.core.network.interceptor.DebugHttpLoggingLogger
import org.p2p.core.network.interceptor.RpcInterceptor
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER

object NetworkCoreModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 30L

    override fun create(): Module = module {
        single(named(RPC_RETROFIT_QUALIFIER)) {
            val environment = get<NetworkEnvironmentManager>().loadCurrentEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "Rpc",
                interceptor = RpcInterceptor(get(), get())
            )
        }

        single(named(RPC_JSON_QUALIFIER)) { GsonProvider().provide() }

        single<Gson> {
            val transactionErrorTypeAdapter = RpcTransactionErrorTypeAdapter(RpcTransactionInstructionErrorParser())
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .registerTypeAdapter(Base58String::class.java, Base58TypeAdapter)
                .registerTypeAdapter(Base64String::class.java, Base64TypeAdapter)
                .registerTypeAdapter(RpcTransactionError::class.java, transactionErrorTypeAdapter)
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }
        singleOf(::NetworkServicesUrlProvider)
    }

    fun Scope.getRetrofit(
        baseUrl: String,
        tag: String? = "OkHttpClient",
        interceptor: Interceptor?
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .client(getClient(tag = tag, interceptor = interceptor))
            .build()
    }

    fun Scope.getRetrofit(
        baseUrl: String,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .client(client)
            .build()
    }

    fun Scope.httpLoggingInterceptor(logTag: String): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(DebugHttpLoggingLogger(gson = get(), logTag = logTag)).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun Scope.getClient(
        readTimeoutSec: Long = DEFAULT_READ_TIMEOUT_SECONDS,
        connectTimeoutSec: Long = DEFAULT_CONNECT_TIMEOUT_SECONDS,
        tag: String?,
        interceptor: Interceptor? = null,
        clientProtocols: List<Protocol>? = null
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .apply {
                if (interceptor != null) {
                    addInterceptor(interceptor)
                }
                if (clientProtocols != null) {
                    protocols(clientProtocols)
                }
                if (BuildConfig.DEBUG && !tag.isNullOrBlank()) {
                    addInterceptor(httpLoggingInterceptor(tag))
                }
                if (BuildConfig.CRASHLYTICS_ENABLED) {
                    addInterceptor(CrashHttpLoggingInterceptor())
                }
            }
            .addNetworkInterceptor(ContentTypeInterceptor())
            .build()
    }
}
