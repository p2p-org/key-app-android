package org.p2p.wallet.infrastructure.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

private const val HEADER_CHANNEL_ID_NAME = "CHANNEL_ID"
private const val HEADER_CHANNEL_ID_VALUE = "CHANNEL_ID"

class GatewayServiceInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return chain.proceed(
            request.newBuilder()
                .addHeader(HEADER_CHANNEL_ID_NAME, HEADER_CHANNEL_ID_VALUE)
                .build()
        )
    }
}
