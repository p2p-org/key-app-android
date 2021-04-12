package com.p2p.wallet.common.di

import com.google.gson.GsonBuilder
import com.p2p.wallet.dashboard.api.RetrofitService
import com.p2p.wallet.infrastructure.persistence.PreferenceService
import com.p2p.wallet.utils.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import org.p2p.solanaj.rpc.RpcClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CommonModule : InjectionModule {

    override fun create() = module {
        single { GsonBuilder().create() }
        single {
            val get = get<PreferenceService>()
            val selectedCluster = get.getSelectedCluster()
            RpcClient(selectedCluster)
        }
        single<Retrofit> {
            Retrofit.Builder()
                .baseUrl("https://serum-api.bonfida.com/")
                .addConverterFactory(GsonConverterFactory.create(get()))
                .apply {
                    client(
                        OkHttpClient.Builder()
                            .addInterceptor(HeaderInterceptor())
                            .addInterceptor(
                                HttpLoggingInterceptor().apply {
                                    level = HttpLoggingInterceptor.Level.BODY
                                }
                            )
                            .build()
                    )
                }
                .build()
        }

        single<RetrofitService> { get<Retrofit>().create(RetrofitService::class.java) }
    }
}