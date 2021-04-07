package com.p2p.wowlet

import android.app.Application
import com.p2p.wowlet.auth.AuthModule
import com.p2p.wowlet.di.apiModule
import com.p2p.wowlet.di.databaseModule
import com.p2p.wowlet.di.repositoryModule
import com.p2p.wowlet.di.viewModule
import com.p2p.wowlet.domain.interactorsModule
import com.p2p.wowlet.infrastructure.InfrastructureModule
import com.p2p.wowlet.root.RootModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.KoinContextHandler
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        KoinContextHandler.stop()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    apiModule,
                    viewModule,
                    databaseModule,
                    repositoryModule,
                    interactorsModule,
                    RootModule.create(),
                    AuthModule.create(),
                    InfrastructureModule.create()
                )
            )
        }
    }
}