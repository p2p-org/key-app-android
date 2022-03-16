package org.p2p.wallet.common.crashlytics

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class CrashHttpLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Timber.i("NETWORK ${request.url} | ${request.method} -->")

        val response = chain.proceed(request)
        val responseLog = buildString {
            append("{ ")
            append("code=${response.code}")
            append(" ")
            append("length=${request.body?.contentLength()}")
            append(" ")
            append("content-type=${request.body?.contentType()}")
            append(" }")
        }
        Timber.i("NETWORK ${request.url} | ${request.method} <-- $responseLog")

        return response
    }
}