package org.p2p.token.service.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.Executors
import org.p2p.core.common.di.InjectionModule

internal object TokenServiceDatabaseModule : InjectionModule {

    override fun create(): Module = module {
        single<TokenServiceDatabase> {
            Room.databaseBuilder(androidContext(), TokenServiceDatabase::class.java, TokenServiceDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(Executors.newCachedThreadPool())
                .build()
        }
        single { get<TokenServiceDatabase>().tokenServicePriceDao() }
    }
}
