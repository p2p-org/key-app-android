package org.p2p.wallet.infrastructure.network

import android.content.res.Resources
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
import org.p2p.wallet.common.crashlytics.CrashHttpLoggingInterceptor
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.HomeModule.MOONPAY_QUALIFIER
import org.p2p.wallet.home.model.BigDecimalTypeAdapter
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.DebugHttpLoggingLogger
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayErrorInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.RpcInterceptor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.push_notifications.PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER
import org.p2p.wallet.rpc.RpcModule.RPC_RETROFIT_QUALIFIER
import org.p2p.wallet.updates.ConnectionStateProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.InputStream
import java.math.BigDecimal
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object NetworkModule : InjectionModule {

    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 60L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 60L

    private const val SSL_CERT_TAG = "SSL_CERT"

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
            val moonPayApiUrl = androidContext().getString(R.string.moonpayBaseUrl)
            getRetrofit(
                baseUrl = moonPayApiUrl,
                tag = "Moonpay",
                resources = get(),
                interceptor = MoonpayErrorInterceptor(get())
            )
        }

        single(named(RPC_RETROFIT_QUALIFIER)) {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val rpcApiUrl = environment.endpoint
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "Rpc",
                resources = get(),
                interceptor = RpcInterceptor(get(), get())
            )
        }

        single(named(NOTIFICATION_SERVICE_RETROFIT_QUALIFIER)) {
            val endpoint = androidContext().getString(R.string.notification_service_url)
            getRetrofit(
                baseUrl = endpoint,
                tag = "NotificationService",
                resources = get(),
                interceptor = null
            )
        }
    }

    fun Scope.getRetrofit(
        baseUrl: String,
        tag: String = "OkHttpClient",
        resources: Resources,
        interceptor: Interceptor?
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .client(getClient(tag, resources, interceptor))
            .build()
    }

    fun Scope.httpLoggingInterceptor(logTag: String): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(DebugHttpLoggingLogger(get(), logTag)).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun Scope.getClient(tag: String, resources: Resources, interceptor: Interceptor? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                try {
                    createCertificate(resources.openRawResource(R.raw.cert))?.let { sslContext ->
                        systemDefaultTrustManager()?.let { trustManager ->
                            Timber.tag(SSL_CERT_TAG).i("SslSocketFactory successfully added with cert")
                            sslSocketFactory(sslContext.socketFactory, trustManager)
                        }
                    }
                } catch (e: Exception) {
                    if (!BuildConfig.DEBUG) {
                        Timber.tag(SSL_CERT_TAG).e(e, "Error on opening SSL cert")
                    }
                }
                if (BuildConfig.CRASHLYTICS_ENABLED) {
                    addInterceptor(CrashHttpLoggingInterceptor())
                }

                if (interceptor != null) {
                    addInterceptor(interceptor)
                }

                if (BuildConfig.DEBUG) {
                    addInterceptor(httpLoggingInterceptor(tag))
                }
            }
            .addNetworkInterceptor(ContentTypeInterceptor())
            .build()
    }

    private fun createCertificate(trustedCertificateIS: InputStream): SSLContext? {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val ca: Certificate = trustedCertificateIS.use { trustedCertificateIS ->
            cf.generateCertificate(trustedCertificateIS)
        }

        // creating a KeyStore containing our trusted CAs
        val keyStoreType: String = KeyStore.getDefaultType()
        val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        // creating a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)

        // creating an SSLSocketFactory that uses our TrustManager
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext
    }

    private fun systemDefaultTrustManager(): X509TrustManager? {
        return try {
            val trustManagerFactory: TrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + trustManagers.contentToString()
            }
            trustManagers[0] as X509TrustManager
        } catch (e: GeneralSecurityException) {
            // The system has no TLS. Just give up.
            throw AssertionError()
        }
    }
}
