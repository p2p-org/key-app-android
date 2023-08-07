package org.p2p.core.crashlytics.helpers

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.net.SocketTimeoutException
import org.p2p.core.utils.bodyAsString

private const val TAG = "CrashHttpLoggingInterceptor"

class CrashHttpLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val rpcMethodName: String? = getRpcMethodName(request)
        val requestLog = createRequestLog(request, rpcMethodName)
        Timber.tag(TAG).i(requestLog)

        val response = try {
            chain.proceed(request)
        } catch (socketTimeout: SocketTimeoutException) {
            Timber.i("Failed with socket timeout: ${request.url}; $rpcMethodName")
            throw socketTimeout
        }

        val responseLog = createResponseLog(response, rpcMethodName)
        Timber.tag(TAG).i(responseLog)

        val responseBody = response.bodyAsString()
        val responseBodySize = responseBody.length
        if (responseBodySize < 7_000) {
            if (!response.isSuccessful || responseBody.contains("error")) {
                // log to crash facades !200 error bodies
                Timber.tag(TAG).i(responseBody)
            } else {
                Timber.tag(TAG).d(responseBody)
            }
        } else {
            Timber.i("Skipping logging response body, it's too big: $responseBodySize")
        }

        return response
    }

    private fun createRequestLog(request: Request, rpcMethodName: String?): String = buildString {
        append("NETWORK ${request.url} | ")

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

    private fun createResponseLog(response: Response, rpcMethodName: String?): String = buildString {
        append("NETWORK ${response.request.url} | ")
        if (rpcMethodName != null) {
            append(rpcMethodName)
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
