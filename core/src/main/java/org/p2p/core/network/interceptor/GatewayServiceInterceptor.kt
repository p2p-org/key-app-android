package org.p2p.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

private const val HEADER_CHANNEL_ID_NAME = "CHANNEL_ID"
private const val HEADER_CHANNEL_ID_VALUE = "P2PWALLET_MOBILE"

private class RequestNoVpnError(response: Response) : IOException(
    "message=${response.message};code=${response.code};url=${response.request.url}"
)

class GatewayServiceInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().apply {
            newBuilder()
                .addHeader(HEADER_CHANNEL_ID_NAME, HEADER_CHANNEL_ID_VALUE)
                .build()
        }

        val response = chain.proceed(request)

        return if (response.code == HttpsURLConnection.HTTP_FORBIDDEN) {
            throw RequestNoVpnError(response)
        } else {
            response
        }
    }
}
