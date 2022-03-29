package org.p2p.wallet.common.crashlytics

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.p2p.wallet.utils.bodyAsString
import timber.log.Timber

class CrashHttpLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestLog = createRequestLog(request)
        Timber.i(requestLog)

        val response = chain.proceed(request)
        val responseLog = createResponseLog(response)
        Timber.i(responseLog)

        return response
    }

    private fun createRequestLog(request: Request): String = buildString {
        append("NETWORK ${request.url} | ")

        if (request.url.host.contains("rpcpool")) {
            getRpcMethodName(request)
                ?.let { append("$it | ") }
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
        if (response.request.url.host.contains("rpcpool")) {
            getRpcMethodName(response.request)
                ?.let { append("$it ") }
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
