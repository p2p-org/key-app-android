package com.p2p.wowlet.di

import com.p2p.wowlet.dataservice.RetrofitService
import com.p2p.wowlet.datastore.DashboardRepository
import com.p2p.wowlet.datastore.DetailActivityRepository
import com.p2p.wowlet.datastore.LocalDatabaseRepository
import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.datastore.TermAndConditionRepository
import com.p2p.wowlet.datastore.WowletApiCallRepository
import com.p2p.wowlet.repository.DashboardRepositoryImpl
import com.p2p.wowlet.repository.DetailActivityRepositoryImpl
import com.p2p.wowlet.repository.LocalDatabaseRepositoryImpl
import com.p2p.wowlet.repository.PreferenceServiceImpl
import com.p2p.wowlet.repository.TermAndConditionRepositoryImpl
import com.p2p.wowlet.repository.WowletApiCallRepositoryImpl
import com.p2p.wowlet.utils.HeaderInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import org.p2p.solanaj.rpc.RpcClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val apiModule = module {
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

val repositoryModule = module {
    single<WowletApiCallRepository> { WowletApiCallRepositoryImpl(get(), get()) }
    single<PreferenceService>(createdAtStart = true) { PreferenceServiceImpl(get(), get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
    single<TermAndConditionRepository> { TermAndConditionRepositoryImpl() }
    single<DetailActivityRepository> { DetailActivityRepositoryImpl(get()) }
    single<LocalDatabaseRepository> { LocalDatabaseRepositoryImpl(get()) }
}