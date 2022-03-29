package org.p2p.wallet.user

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.api.CompareApi
import org.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_CONNECT_TIMEOUT_SECONDS
import org.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_READ_TIMEOUT_SECONDS
import org.p2p.wallet.infrastructure.network.NetworkModule.httpLoggingInterceptor
import org.p2p.wallet.infrastructure.network.interceptor.CompareTokenInterceptor
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.repository.UserAccountRemoteRepository
import org.p2p.wallet.user.repository.UserAccountRepository
import org.p2p.wallet.user.repository.UserInMemoryRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRemoteRepository
import org.p2p.wallet.user.repository.UserRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object UserModule : InjectionModule {

    private const val CRYPTO_COMPARE_QUALIFIER = "cryptocompare.com"

    override fun create() = module {

        single(named(CRYPTO_COMPARE_QUALIFIER)) {
            val client = createOkHttpClient()
                .addInterceptor(CompareTokenInterceptor())
                .apply { if (BuildConfig.DEBUG) addInterceptor(httpLoggingInterceptor("CryptoCompare")) }
                .build()

            Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.compareBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
        }

        single {
            val client = createOkHttpClient()
                .apply {
                    if (BuildConfig.DEBUG) {
                        val interceptor = HttpLoggingInterceptor()
                        interceptor.level = HttpLoggingInterceptor.Level.BASIC
                        addInterceptor(interceptor)
                    }
                }
                .build()
            Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.solanaTokensBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(SolanaApi::class.java)
        }

        factory {
            UserRemoteRepository(get(), get(), get(), get(), get(), get())
        } bind UserRepository::class

        factory { get<Retrofit>(named(CRYPTO_COMPARE_QUALIFIER)).create(CompareApi::class.java) }

        single { UserInMemoryRepository() } bind UserLocalRepository::class
        factory { UserInteractor(get(), get(), get(), get(), get()) }

        single { UserAccountRemoteRepository(get()) } bind UserAccountRepository::class
    }

    private fun createOkHttpClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}
