package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.core.wrapper.eth.toByteArray
import org.p2p.solanaj.utils.TweetNaclFast
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
        val currentUnixTime = System.currentTimeMillis()
        val signedUnixTime = TweetNaclFast.Signature(byteArrayOf(), tokenKeyProvider.keyPair.copyOf())
            .detached(currentUnixTime.toByteArray())
            .toBase64Instance()

        return "$currentUnixTime:$signedUnixTime"
    }
}
