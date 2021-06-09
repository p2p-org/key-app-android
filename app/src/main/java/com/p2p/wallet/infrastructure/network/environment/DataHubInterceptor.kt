package com.p2p.wallet.infrastructure.network.environment

import android.content.Context
import com.p2p.wallet.R
import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.solanaj.rpc.Environment

class DataHubInterceptor(
    context: Context
) : Interceptor {

    private val apiKey = context.getString(R.string.datahubApiKey)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return if (request.url.toString().startsWith(Environment.DATAHUB.endpoint)) {
            val newRequest = request.newBuilder()
                .apply { addHeader("Authorization", apiKey) }
                .build()

            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}