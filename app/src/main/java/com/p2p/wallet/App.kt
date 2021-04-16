package com.p2p.wallet

import android.app.Application
import com.p2p.wallet.auth.AuthModule
import com.p2p.wallet.restore.BackupModule
import com.p2p.wallet.common.di.CommonModule
import com.p2p.wallet.dashboard.DashboardModule
import com.p2p.wallet.infrastructure.InfrastructureModule
import com.p2p.wallet.root.RootModule
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
                    AuthModule.create(),
                    RootModule.create(),
                    BackupModule.create(),
                    DashboardModule.create(),
                    CommonModule.create(),
                    InfrastructureModule.create()
                )
            )
        }
    }
}