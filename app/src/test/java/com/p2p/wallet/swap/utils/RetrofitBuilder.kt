package com.p2p.wallet.swap.utils

import com.google.gson.Gson
import com.p2p.wallet.infrastructure.network.NetworkModule
import com.p2p.wallet.infrastructure.network.interceptor.ContentTypeInterceptor
import okhttp3.OkHttpClient
import org.p2p.solanaj.rpc.Environment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    private val gson: Gson = Gson()

    fun getRetrofit(url: String? = null): Retrofit {
        val client = OkHttpClient.Builder()
            .readTimeout(NetworkModule.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(NetworkModule.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addNetworkInterceptor(ContentTypeInterceptor())
            .build()

        val baseUrl = if (url.isNullOrEmpty()) Environment.SOLANA.endpoint else url

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }
}