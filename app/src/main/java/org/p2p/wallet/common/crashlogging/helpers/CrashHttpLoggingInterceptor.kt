package org.p2p.wallet.common.crashlogging.helpers

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.net.SocketTimeoutException
import org.p2p.wallet.utils.bodyAsString

private const val TAG = "CrashHttpLoggingInterceptor"

class CrashHttpLoggingInterceptor : Interceptor {

    private var rpcMethodName: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestLog = createRequestLog(request)
        Timber.tag(TAG).i(requestLog)

        val response = try {
            chain.proceed(request)
        } catch (socketTimeout: SocketTimeoutException) {
            Timber.i("Failed with socket timeout: ${request.url}")
            throw socketTimeout
        }

        val responseLog = createResponseLog(response)
        Timber.tag(TAG).i(responseLog)

        val responseBody = response.peekBody(Long.MAX_VALUE).string()
        val responseBodySize = responseBody.length
        if (responseBodySize < 10_000) {
            Timber.tag(TAG).d(responseBody)
        } else {
            Timber.i("Skipping logging response body, it's too big: $responseBodySize")
        }

        return response
    }

    private fun createRequestLog(request: Request): String = buildString {
        append("NETWORK ${request.url} | ")

        rpcMethodName = getRpcMethodName(request)
        if (rpcMethodName != null) {
            append("$rpcMethodName | ")
        }

        append("${request.method} ")
        append("-->")
    }

    private fun getRpcMethodName(request: Request): String? = kotlin.runCatching {
        request.bodyAsString()
    }
        .mapCatching { JSONObject(it).getString("method") }
        .getOrNull()

    private fun createResponseLog(response: Response) = buildString {
        append("NETWORK ${response.request.url} | ")
        if (rpcMethodName != null) {
            append(rpcMethodName)
            rpcMethodName = null
        }

        append("<-- ")

        append("{")
        append("code=${response.code}")
        append(" ")
        append("length=${response.request.body?.contentLength()}")
        append(" ")
        append("content-type=${response.request.body?.contentType()}")
        append("}")
    }
}
