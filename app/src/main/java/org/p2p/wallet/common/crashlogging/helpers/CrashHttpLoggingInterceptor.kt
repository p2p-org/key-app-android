package org.p2p.wallet.common.crashlogging.helpers

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.p2p.wallet.utils.bodyAsString
import timber.log.Timber

private const val TAG = "CrashHttpLoggingInterceptor"

class CrashHttpLoggingInterceptor : Interceptor {

    private var rpcMethodName: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestLog = createRequestLog(request)
        Timber.tag(TAG).i(requestLog)
        Timber.tag(TAG).d(request.bodyAsString())

        val response = chain.proceed(request)
        val responseLog = createResponseLog(response)
        Timber.tag(TAG).i(responseLog)
        Timber.tag(TAG).d(response.peekBody(Long.MAX_VALUE).string())

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
