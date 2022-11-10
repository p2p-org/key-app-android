package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class CompareTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return chain.proceed(request.newBuilder().build())
    }
}
