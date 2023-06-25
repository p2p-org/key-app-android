package org.p2p.wallet.infrastructure.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.math.BigDecimal
import org.p2p.core.BuildConfig
import org.p2p.core.rpc.RpcApi
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.GatewayServiceModule.FACADE_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.auth.username.di.RegisterUsernameServiceModule.REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.bridge.BridgeModule
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.core.network.gson.Base58TypeAdapter
import org.p2p.core.network.gson.Base64TypeAdapter
import org.p2p.core.network.gson.BigDecimalTypeAdapter
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.data.transactionerrors.RpcTransactionErrorTypeAdapter
import org.p2p.core.network.data.transactionerrors.RpcTransactionInstructionErrorParser
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.network.interceptor.GatewayServiceInterceptor
import org.p2p.core.network.interceptor.RpcSolanaInterceptor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SettingsNetworkListFeatureToggle
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayInterceptor
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.network.ssl.CertificateManager
import org.p2p.wallet.jupiter.JupiterModule.JUPITER_RETROFIT_QUALIFIER
import org.p2p.wallet.push_notifications.PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.rpc.RpcModule.REN_POOL_RETROFIT_QUALIFIER
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.core.crypto.Base58String

object NetworkModule : InjectionModule {

    enum class MoonpayRetrofitQualifier {
        CLIENT_SIDE_MOONPAY,
        SERVER_SIDE_PROXY
    }

    override fun create() = module {
        single {
            NetworkEnvironmentManager(
                networkEnvironmentStorage = get(),
                crashLogger = get(),
                networksFromRemoteConfig = get<SettingsNetworkListFeatureToggle>().getAvailableEnvironments()
            )
        }
        singleOf(::TokenKeyProvider)
        singleOf(::SeedPhraseProvider)
        singleOf(::CertificateManager)

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

        singleOf(::NetworkConnectionStateProvider)

        createMoonpayNetworkModule()

        single<RpcApi> {
            getRetrofit(
                // no need for baseUrl here, we pass URL inside RpcApi
                baseUrl = "http://localhost/",
                tag = "RpcApi",
                interceptor = null
            )
                .create(RpcApi::class.java)
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
