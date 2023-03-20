package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.net.SocketTimeoutException

class ContentTypeInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json")
        return try {
            chain.proceed(requestBuilder.build())
        } catch (socketTimeout: SocketTimeoutException) {
            Timber.i("SocketTimeoutException for ${chain.request().url}")
            throw socketTimeout
        }
    }
}
