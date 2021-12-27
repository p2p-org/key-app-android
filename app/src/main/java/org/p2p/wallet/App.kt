package org.p2p.wallet

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.main.MainModule
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.qr.QrModule
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.root.ui.RootActivity
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.transaction.di.TransactionModule
import org.p2p.wallet.user.UserModule
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        setupKoin()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.CRASHLYTICS_ENABLED)

        AppNotificationManager.createNotificationChannels(this)

        AndroidThreeTen.init(this)
        DebugDrawer.init(this)
        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()
    }

    private fun setupKoin() {
        GlobalContext.stopKoin()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    AuthModule.create(),
                    RootModule.create(),
                    BackupModule.create(),
                    UserModule.create(),
                    MainModule.create(),
                    RenBtcModule.create(),
                    NetworkModule.create(),
                    QrModule.create(),
                    HistoryModule.create(),
                    SettingsModule.create(),
                    SwapModule.create(),
                    RpcModule.create(),
                    InfrastructureModule.create(),
                    TransactionModule.create(),
                    createAppModule()
                )
            )
        }
    }

    private fun createAppModule(): Module = module {
        single { AppScope() }
        single {
            AppRestarter {
                restart()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        } bind AppRestarter::class
    }

    private fun restart() {
        setupKoin()
        RootActivity
            .createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let { startActivity(it) }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}