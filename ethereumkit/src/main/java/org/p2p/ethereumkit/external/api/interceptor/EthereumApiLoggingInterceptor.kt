package org.p2p.ethereumkit.external.api.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import timber.log.Timber
import java.net.SocketTimeoutException
import kotlin.random.Random

private const val TAG = "EthApi"

class EthereumApiLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val rpcMethodName: String? = getRpcMethodName(request)
        val uniqueNumber: Int = Random.nextInt(request.body?.contentLength()?.toInt() ?: 1)

        val requestLog = createRequestLog(request, rpcMethodName, uniqueNumber)
        Timber.tag(TAG).i(requestLog)

        val response = try {
            chain.proceed(request)
        } catch (socketTimeout: SocketTimeoutException) {
            Timber.i("Failed with socket timeout: ${request.url}")
            throw socketTimeout
        }

        val responseLog = createResponseLog(response, rpcMethodName, uniqueNumber)
        Timber.tag(TAG).i(responseLog)

        return response
    }

    private fun createRequestLog(request: Request, rpcMethodName: String?, uniqueNumber: Int): String = buildString {
        append("NETWORK($uniqueNumber) ${request.url} | ")

        if (rpcMethodName != null) {
            append("$rpcMethodName | ")
        }

        append("${request.method} ")
        append("-->")
    }

    private fun getRpcMethodName(request: Request): String? = kotlin.runCatching {
        val requestCopy: Request = request.newBuilder().build()
        val buffer = Buffer()
        requestCopy.body?.writeTo(buffer)
        buffer.readUtf8()
    }
        .mapCatching { JSONObject(it).getString("method") }
        .getOrNull()

    private fun createResponseLog(response: Response, rpcMethodName: String?, uniqueNumber: Int) = buildString {
        append("NETWORK($uniqueNumber) ${response.request.url} | ")

        if (rpcMethodName != null) {
            append(rpcMethodName)
        }

        append(" <-- ")

        append("{")
        append("code=${response.code}")
        append(" ")
        append("length=${response.request.body?.contentLength()}")
        append(" ")
        append("content-type=${response.request.body?.contentType()}")
        append("}")
    }
}
