package org.p2p.wallet.infrastructure.network

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
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
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.GatewayServiceModule.FACADE_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.auth.username.di.RegisterUsernameServiceModule.REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.bridge.BridgeModule
import org.p2p.wallet.common.crashlogging.helpers.CrashHttpLoggingInterceptor
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Base58TypeAdapter
import org.p2p.wallet.home.model.Base64TypeAdapter
import org.p2p.wallet.home.model.BigDecimalTypeAdapter
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.DebugHttpLoggingLogger
import org.p2p.wallet.infrastructure.network.interceptor.GatewayServiceInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.RpcInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.RpcSolanaInterceptor
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.network.ssl.CertificateManager
import org.p2p.wallet.jupiter.JupiterModule.JUPITER_RETROFIT_QUALIFIER
import org.p2p.wallet.push_notifications.PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.rpc.RpcModule.REN_POOL_RETROFIT_QUALIFIER
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.wallet.utils.Base58String

object NetworkModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 30L

    enum class MoonpayRetrofitQualifier {
        CLIENT_SIDE_MOONPAY,
        SERVER_SIDE_PROXY
    }

    override fun create() = module {
        single { NetworkServicesUrlProvider(get(), get()) }
        single { NetworkEnvironmentManager(get(), get(), get()) }
        singleOf(::TokenKeyProvider)
        singleOf(::SeedPhraseProvider)
        single { CertificateManager(get(), get()) }

        single {
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .registerTypeAdapter(Base58String::class.java, Base58TypeAdapter)
                .registerTypeAdapter(Base64String::class.java, Base64TypeAdapter)
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }

        single { NetworkConnectionStateProvider(get()) }

        createMoonpayNetworkModule()

        single(named(RPC_RETROFIT_QUALIFIER)) {
            val environment = get<NetworkEnvironmentManager>().loadCurrentEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "Rpc",
                interceptor = RpcInterceptor(get(), get())
            )
        }
        single(named(REN_POOL_RETROFIT_QUALIFIER)) {
            val environment = get<NetworkEnvironmentManager>().loadRpcEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "RpcSolana",
                interceptor = RpcSolanaInterceptor(get())
            )
        }

        single(named(BridgeModule.BRIDGE_RETROFIT_QUALIFIER)) {
            val url = get<NetworkServicesUrlProvider>()
            getRetrofit(
                baseUrl = url.loadBridgesServiceEnvironment().baseUrl,
                tag = "RpcBridge",
                interceptor = null
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
                baseUrl = androidContext().getString(R.string.web3AuthServiceBaseUrl),
                tag = "FacadeService",
                interceptor = GatewayServiceInterceptor()
            )
        }

        single(named(REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER)) {
            val environmentManager = get<NetworkServicesUrlProvider>()
            val baseUrl = environmentManager.loadNameServiceEnvironment().baseUrl
            getRetrofit(
                baseUrl = baseUrl,
                tag = "RegisterUsernameService",
                interceptor = null
            )
        }

        single(named(JUPITER_RETROFIT_QUALIFIER)) {
            val baseUrl = androidContext().getString(R.string.jupiterQuoteBaseUrl)
            getRetrofit(
                baseUrl = baseUrl,
                tag = "JupiterQuoteService",
                interceptor = null
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
        readTimeOut: Long = DEFAULT_READ_TIMEOUT_SECONDS,
        connectTimeOut: Long = DEFAULT_CONNECT_TIMEOUT_SECONDS,
        tag: String?,
        interceptor: Interceptor? = null
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (interceptor != null) {
                    addInterceptor(interceptor)
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

    private fun Module.createMoonpayNetworkModule() {
        singleOf(::MoonpayInterceptor)
        single(named(MoonpayRetrofitQualifier.CLIENT_SIDE_MOONPAY)) {
            val url = get<NetworkServicesUrlProvider>().loadMoonpayEnvironment().baseClientSideUrl
            getRetrofit(
                baseUrl = url,
                tag = "MoonpayClientSide",
                interceptor = get<MoonpayInterceptor>()
            )
        }

        single(named(MoonpayRetrofitQualifier.SERVER_SIDE_PROXY)) {
            val url = get<NetworkServicesUrlProvider>().loadMoonpayEnvironment().baseServerSideUrl
            getRetrofit(
                baseUrl = url,
                tag = "MoonpayServerSide",
                interceptor = get<MoonpayInterceptor>()
            )
        }
    }
}
