package org.p2p.wallet.infrastructure.network

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.GatewayServiceModule.FACADE_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.common.crashlogging.helpers.CrashHttpLoggingInterceptor
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.HomeModule.MOONPAY_QUALIFIER
import org.p2p.wallet.home.model.Base58TypeAdapter
import org.p2p.wallet.home.model.BigDecimalTypeAdapter
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.DebugHttpLoggingLogger
import org.p2p.wallet.infrastructure.network.interceptor.GatewayServiceInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayErrorInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.RpcInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.RpcSolanaInterceptor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.network.ssl.CertificateManager
import org.p2p.wallet.push_notifications.PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.rpc.RpcModule.RPC_RETROFIT_QUALIFIER
import org.p2p.wallet.rpc.RpcModule.RPC_SOLANA_RETROFIT_QUALIFIER
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.utils.Base58String
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

object NetworkModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 30L

    override fun create() = module {
        single { NetworkServicesUrlProvider(get(), get()) }
        single { NetworkEnvironmentManager(get(), get()) }
        single { TokenKeyProvider(get()) }
        single { CertificateManager(get(), get()) }

        single {
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .registerTypeAdapter(Base58String::class.java, Base58TypeAdapter)
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }

        single { ConnectionStateProvider(get()) }

        single(named(MOONPAY_QUALIFIER)) {
            val moonPayApiUrl = androidContext().getString(R.string.moonpayBaseUrl)
            getRetrofit(
                baseUrl = moonPayApiUrl,
                tag = "Moonpay",
                interceptor = MoonpayErrorInterceptor(get())
            )
        }

        single(named(RPC_RETROFIT_QUALIFIER)) {
            val environment = get<NetworkEnvironmentManager>().loadCurrentEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "Rpc",
                interceptor = RpcInterceptor(get(), get())
            )
        }
        single(named(RPC_SOLANA_RETROFIT_QUALIFIER)) {
            val environment = get<NetworkEnvironmentManager>().loadRpcEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "RpcSolana",
                interceptor = RpcSolanaInterceptor(get())
            )
        }

        single(named(NOTIFICATION_SERVICE_RETROFIT_QUALIFIER)) {
            val url = get<NetworkServicesUrlProvider>().loadNotificationServiceEnvironment().baseUrl
            getRetrofit(
                baseUrl = url,
                tag = "NotificationService",
                interceptor = null
            )
        }

        single(named(FACADE_SERVICE_RETROFIT_QUALIFIER)) {
            getRetrofit(
                // TODO
                baseUrl = "http://35.234.120.240:9090/",
                tag = "FacadeService",
                interceptor = GatewayServiceInterceptor()
            )
        }
    }

    fun Scope.getRetrofit(
        baseUrl: String,
        tag: String? = "OkHttpClient",
        interceptor: Interceptor?
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .client(getClient(tag, interceptor))
            .build()
    }

    fun Scope.httpLoggingInterceptor(logTag: String): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(DebugHttpLoggingLogger(get(), logTag)).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun Scope.getClient(tag: String?, interceptor: Interceptor? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                val certificateManager: CertificateManager = get()
                certificateManager.setCertificate(this)
                if (BuildConfig.CRASHLYTICS_ENABLED) {
                    addInterceptor(CrashHttpLoggingInterceptor())
                }

                if (interceptor != null) {
                    addInterceptor(interceptor)
                }

                if (BuildConfig.DEBUG && !tag.isNullOrBlank()) {
                    addInterceptor(httpLoggingInterceptor(tag))
                }
            }
            .addNetworkInterceptor(ContentTypeInterceptor())
            .build()
    }
}
