package com.p2p.wallet.user

import android.content.Context
import com.google.gson.Gson
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_CONNECT_TIMEOUT_SECONDS
import com.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_READ_TIMEOUT_SECONDS
import com.p2p.wallet.infrastructure.network.NetworkModule.createLoggingInterceptor
import com.p2p.wallet.infrastructure.network.interceptor.CompareTokenInterceptor
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.user.api.SolanaApi
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.user.repository.UserInMemoryRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import com.p2p.wallet.user.repository.UserRepositoryImpl
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object UserModule : InjectionModule {

    private const val CRYPTO_COMPARE_QUALIFIER = "cryptocompare.com"

    override fun create() = module {

        single(named(CRYPTO_COMPARE_QUALIFIER)) {
            val client = createOkHttpClient()
                .addInterceptor(CompareTokenInterceptor(get()))
                .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor("CryptoCompare")) }
                .build()

            Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.compareBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
        }

        single {
            val client = createOkHttpClient()
                .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor("SolanaApi")) }
                .build()
            Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.solanaTokensBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(SolanaApi::class.java)
        }

        factory {
            UserRepositoryImpl(get(), get(), get(), get(), get(), get())
        } bind UserRepository::class

        factory { get<Retrofit>(named(CRYPTO_COMPARE_QUALIFIER)).create(CompareApi::class.java) }

        single { UserInMemoryRepository() } bind UserLocalRepository::class
        factory { UserInteractor(get(), get(), get(), get()) }
    }

    private fun createOkHttpClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}