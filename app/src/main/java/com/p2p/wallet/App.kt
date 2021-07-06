package com.p2p.wallet

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.p2p.wallet.auth.AuthModule
import com.p2p.wallet.common.di.CommonModule
import com.p2p.wallet.infrastructure.InfrastructureModule
import com.p2p.wallet.infrastructure.network.NetworkModule
import com.p2p.wallet.main.MainModule
import com.p2p.wallet.qr.QrModule
import com.p2p.wallet.restore.BackupModule
import com.p2p.wallet.root.RootModule
import com.p2p.wallet.settings.SettingsModule
import com.p2p.wallet.settings.interactor.ThemeInteractor
import com.p2p.wallet.swap.SwapModule
import com.p2p.wallet.token.TokenModule
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

        AndroidThreeTen.init(this)

        KoinContextHandler.get().get<ThemeInteractor>().applyCurrentNightMode()
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
                    TokenModule.create(),
                    SettingsModule.create(),
                    SwapModule.create(),
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