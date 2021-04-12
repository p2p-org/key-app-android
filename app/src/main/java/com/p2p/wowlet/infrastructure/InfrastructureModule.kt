package com.p2p.wowlet.infrastructure

import android.content.Context
import androidx.room.Room
import com.p2p.wowlet.common.di.InjectionModule
import com.p2p.wowlet.dashboard.interactor.NetworksInteractor
import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.infrastructure.persistence.PreferenceServiceImpl
import com.p2p.wowlet.infrastructure.persistence.WalletDatabase
import org.koin.dsl.module

object InfrastructureModule : InjectionModule {

    override fun create() = module {
        single {
            Room.databaseBuilder(get(), WalletDatabase::class.java, WalletDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }

        single { get<WalletDatabase>().walletDAO() }
        single {
            val context = get<Context>()
            val name = "${context.packageName}.prefs"
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        single<PreferenceService>(createdAtStart = true) { PreferenceServiceImpl(get(), get(), get()) }
        factory { NetworksInteractor(get()) }

    }
}