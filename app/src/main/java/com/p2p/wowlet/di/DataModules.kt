package com.p2p.wowlet.di

import android.app.Application
import androidx.room.Room
import com.p2p.wowlet.dao.LocalWalletItemDAO
import com.p2p.wowlet.datastore.DashboardRepository
import com.p2p.wowlet.datastore.DetailActivityRepository
import com.p2p.wowlet.datastore.LocalDatabaseRepository
import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.datastore.TermAndConditionRepository
import com.p2p.wowlet.datastore.WowletApiCallRepository
import com.squareup.moshi.Moshi
import com.p2p.wowlet.database.WalletDatabase
import com.p2p.wowlet.dataservice.RetrofitService
import com.p2p.wowlet.repository.DashboardRepositoryImpl
import com.p2p.wowlet.repository.DetailActivityRepositoryImpl
import com.p2p.wowlet.repository.LocalDatabaseRepositoryImpl
import com.p2p.data.repository.PreferenceServiceImpl
import com.p2p.wowlet.repository.TermAndConditionRepositoryImpl
import com.p2p.wowlet.repository.WowletApiCallRepositoryImpl
import com.p2p.wowlet.util.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import org.p2p.solanaj.rpc.RpcClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val apiModule = module {
    single { Moshi.Builder().build() }
    single<RpcClient> {
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
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .build()
                )
            }
            .build()
    }

    single<RetrofitService> { get<Retrofit>().create(RetrofitService::class.java) }
}

val databaseModule = module {

    fun provideDatabase(application: Application): WalletDatabase {
        return Room.databaseBuilder(application, WalletDatabase::class.java, "wallet.database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun provideDao(database: WalletDatabase): LocalWalletItemDAO {
        return database.walletDAO
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
}

val repositoryModule = module {
    single<WowletApiCallRepository> { WowletApiCallRepositoryImpl(get(), get()) }
    single<PreferenceService>(createdAtStart = true) { PreferenceServiceImpl(get(), get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
    single<TermAndConditionRepository> { TermAndConditionRepositoryImpl() }
    single<DetailActivityRepository> { DetailActivityRepositoryImpl(get()) }
    single<LocalDatabaseRepository> { LocalDatabaseRepositoryImpl(get()) }
}


