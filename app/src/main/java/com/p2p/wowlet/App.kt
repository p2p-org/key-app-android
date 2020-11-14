package com.p2p.wowlet

import android.app.Application

import com.p2p.wowlet.di.viewModule
import com.wowlet.data.di.apiModule
import com.wowlet.data.di.databaseModule
import com.wowlet.data.di.repositoryModule
import com.wowlet.domain.di.interactorsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(modules)
        }
    }

    private val modules = listOf(
        apiModule,
        viewModule,
        databaseModule,
        repositoryModule,
        interactorsModule,
    )
}