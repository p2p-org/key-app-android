package com.p2p.wowlet.common.di

import com.p2p.wowlet.dashboard.api.RetrofitService
import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.utils.HeaderInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import org.p2p.solanaj.rpc.RpcClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CommonModule : InjectionModule {

    override fun create() = module {
        single { Moshi.Builder().build() }
        single {
            val get = get<PreferenceService>()
            val selectedCluster = get.getSelectedCluster()
            RpcClient(selectedCluster)
        }
        single<Retrofit> {
            Retrofit.Builder()
                .baseUrl("https://serum-api.bonfida.com/")
                .addConverterFactory(MoshiConverterFactory.create())
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