package com.p2p.wallet

import android.app.Application
import com.p2p.wallet.auth.AuthModule
import com.p2p.wallet.common.di.CommonModule
import com.p2p.wallet.dashboard.DashboardModule
import com.p2p.wallet.infrastructure.InfrastructureModule
import com.p2p.wallet.infrastructure.network.NetworkModule
import com.p2p.wallet.main.MainModule
import com.p2p.wallet.qr.QrModule
import com.p2p.wallet.restore.BackupModule
import com.p2p.wallet.root.RootModule
import com.p2p.wallet.user.UserModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.KoinContextHandler
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
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
                    UserModule.create(),
                    MainModule.create(),
                    NetworkModule.create(),
                    QrModule.create(),
                    DashboardModule.create(),
                    CommonModule.create(),
                    InfrastructureModule.create()
                )
            )
        }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}