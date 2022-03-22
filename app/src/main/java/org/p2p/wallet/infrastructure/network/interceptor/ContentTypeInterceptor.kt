package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ContentTypeInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json")
        return chain.proceed(requestBuilder.build())
    }
}
