package org.p2p.wallet.infrastructure.network.interceptor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.R

class CompareTokenInterceptor(
    context: Context
) : Interceptor {

    private val publicKey = context.getString(R.string.comparePublicKey)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val newRequest = request.newBuilder().apply {
            addHeader("Authorization", "Apikey $publicKey")
        }

        return chain.proceed(newRequest.build())
    }
}