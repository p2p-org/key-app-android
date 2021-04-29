package com.p2p.wallet.infrastructure.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.BigDecimalTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

object NetworkModule : InjectionModule {

    private const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 60L
    private const val DEFAULT_READ_TIMEOUT_SECONDS = 60L

    override fun create() = module {
        single { TokenKeyProvider(get(), get(), get()) }

        single {
            GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .create()
        }

        single(named("cryptocompare")) {
            val client = createOkHttpClient()
                .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor()) }
                .build()

            Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.compareBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
                .client(client)
                .build()
        }
    }

    private fun Scope.createLoggingInterceptor(): HttpLoggingInterceptor {
        val gson = get<Gson>()
        val okHttpLogTag = "OkHttp"

        val okHttpLogger = object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (!message.startsWith('{') && !message.startsWith('[')) {
                    Timber.tag(okHttpLogTag).d(message)
                    return
                }

                try {
                    val json = JsonParser().parse(message)
                    Timber.tag(okHttpLogTag).d(gson.toJson(json))
                } catch (e: Throwable) {
                    Timber.tag(okHttpLogTag).d(message)
                }
            }
        }
        return HttpLoggingInterceptor(okHttpLogger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun Scope.createOkHttpClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(CompareTokenInterceptor(get()))
}