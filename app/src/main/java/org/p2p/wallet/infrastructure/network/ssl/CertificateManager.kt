package org.p2p.wallet.infrastructure.network.ssl

import android.content.res.Resources
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.p2p.wallet.common.feature_toggles.toggles.remote.SslPinningFeatureToggle
import timber.log.Timber
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val SSL_CERT_TAG = "SSL_CERT"

class CertificateManager(
    private val sslPinningFeatureToggle: SslPinningFeatureToggle,
    val resources: Resources,
) {

    fun setCertificate(builder: OkHttpClient.Builder) {
        // TODO: When ssl will be back, we should add SSL certificate to CI/CD
//        if (sslPinningFeatureToggle.isFeatureEnabled) {
//            try {
//                createCertificate(resources.openRawResource(R.raw.cert)).apply {
//                    systemDefaultTrustManager()?.let { trustManager ->
//                        Timber.tag(SSL_CERT_TAG).i("SslSocketFactory successfully added with cert")
//                        builder.sslSocketFactory(socketFactory, trustManager)
//                    }
//                }
//            } catch (e: Exception) {
//                if (!BuildConfig.DEBUG) {
//                    Timber.tag(SSL_CERT_TAG).e(e, "Error on opening SSL cert")
//                }
//            }
//        }
    }

    private fun createCertificate(trustedCertificateIS: InputStream): SSLContext {
        val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
        val certificate: Certificate = trustedCertificateIS.use { trustedCertificate ->
            certificateFactory.generateCertificate(trustedCertificate)
        }

        // creating a KeyStore containing our trusted CAs
        val keyStoreType: String = KeyStore.getDefaultType()
        val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", certificate)

        // creating a TrustManager that trusts the CAs in our KeyStore
        val trustAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(trustAlgorithm)
        trustManagerFactory.init(keyStore)

        // creating an SSLSocketFactory that uses our TrustManager
        return SSLContext.getInstance(TlsVersion.TLS_1_2.javaName).also { sslContext ->
            sslContext.init(null, trustManagerFactory.trustManagers, null)
        }
    }

    private fun systemDefaultTrustManager(): X509TrustManager? = try {
        val trustManagerFactory: TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers:" + trustManagers.contentToString()
        }
        trustManagers[0] as X509TrustManager
    } catch (e: GeneralSecurityException) {
        Timber.e(e, "Error on getting systemDefaultTrustManager")
        throw AssertionError()
    }
}
