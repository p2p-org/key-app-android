package org.p2p.wallet.user

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_CONNECT_TIMEOUT_SECONDS
import org.p2p.wallet.infrastructure.network.NetworkModule.DEFAULT_READ_TIMEOUT_SECONDS
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

    override fun create() = module {
        networkLayer()

        factory { UserRemoteRepository(get(), get(), get(), get(), get(), get(), get()) } bind UserRepository::class
        single { UserAccountRemoteRepository(get()) } bind UserAccountRepository::class
        single { UserInMemoryRepository() } bind UserLocalRepository::class

        factory { UserInteractor(get(), get(), get(), get(), get(), get()) }
    }

    private fun Module.networkLayer() {
        single<SolanaApi> {
            val client = createOkHttpClient().apply {
                if (BuildConfig.DEBUG) {
                    val interceptor = HttpLoggingInterceptor()
                    interceptor.level = HttpLoggingInterceptor.Level.BASIC
                    addInterceptor(interceptor)
                }
            }
                .build()

            Retrofit.Builder()
                .baseUrl(androidContext().getString(R.string.solanaTokensBaseUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(SolanaApi::class.java)
        }
    }

    private fun createOkHttpClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}
