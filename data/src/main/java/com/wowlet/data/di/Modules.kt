package com.wowlet.data.di

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.wowlet.data.dao.LocalWalletItemDAO
import com.wowlet.data.database.WalletDatabase
import com.wowlet.data.dataservice.RetrofitService
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.DetailActivityRepository
import com.wowlet.data.datastore.LocalDatabaseRepository
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.TermAndConditionRepository
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.data.repository.DashboardRepositoryImpl
import com.wowlet.data.repository.DetailActivityRepositoryImpl
import com.wowlet.data.repository.LocalDatabaseRepositoryImpl
import com.wowlet.data.repository.PreferenceServiceImpl
import com.wowlet.data.repository.TermAndConditionRepositoryImpl
import com.wowlet.data.repository.WowletApiCallRepositoryImpl
import com.wowlet.data.util.HeaderInterceptor
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


