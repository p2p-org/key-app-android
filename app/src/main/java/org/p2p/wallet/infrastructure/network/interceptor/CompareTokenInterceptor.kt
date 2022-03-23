package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.BuildConfig

class CompareTokenInterceptor : Interceptor {

    private val publicKey = BuildConfig.comparePublicKey

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val newRequest = request.newBuilder().apply {
            addHeader("Authorization", "Apikey $publicKey")
        }

        return chain.proceed(newRequest.build())
    }
}
