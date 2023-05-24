package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.Date
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.encodeToBase64
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class StrigaProxyApiInterceptor(
    private val tokenKeyProvider: TokenKeyProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (tokenKeyProvider.publicKey.isBlank()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
            .newBuilder()
            .addHeader("User-PublicKey", tokenKeyProvider.publicKey)
            .addHeader("Signed-Message", generateHeaderSignature())
            .build()

        return chain.proceed(request)
    }

    /**
     * @return format: unix_time:unix_time_signed_by_private_key
     */
    private fun generateHeaderSignature(): String {
        val currentUnixTime = Date().time.toString()

        val signedUnixTime = TweetNaclFast.Signature(byteArrayOf(), tokenKeyProvider.keyPair.copyOf())
            .detached(currentUnixTime.toByteArray())
            .toBase64Instance()
            .base64Value

        return "$currentUnixTime:$signedUnixTime".also {
            println(tokenKeyProvider.publicKey)
            println(tokenKeyProvider.keyPair.encodeToBase64())
        }
    }
}
