package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class StrigaProxyApiInterceptor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val signatureGenerator: StrigaHeaderSignatureGenerator
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (tokenKeyProvider.publicKey.isBlank()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
            .newBuilder()
            .addHeader("User-PublicKey", tokenKeyProvider.publicKey)
            .addHeader("Signed-Message", signatureGenerator.generate(tokenKeyProvider.keyPair))
            .build()

        return chain.proceed(request)
    }
}
