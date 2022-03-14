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
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.TrackerContract
import org.p2p.wallet.common.analytics.TrackerFactory
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.qr.QrModule
import org.p2p.wallet.renbtc.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.root.RootModule
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
        IntercomService.setup(this, BuildConfig.intercomApiKey, BuildConfig.intercomAppId)
        AndroidThreeTen.init(this)
        DebugDrawer.init(this)
        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()
    }

    private fun setupKoin() {
        GlobalContext.stopKoin()
        startKoin{
            androidContext(this@App)
            modules(
                listOf(
                    AuthModule.create(),
                    RootModule.create(),
                    BackupModule.create(),
                    UserModule.create(),
                    HomeModule.create(),
                    RenBtcModule.create(),
                    NetworkModule.create(),
                    QrModule.create(),
                    HistoryModule.create(),
                    SettingsModule.create(),
                    SwapModule.create(),
                    RpcModule.create(),
                    FeeRelayerModule.create(),
                    InfrastructureModule.create(),
                    TransactionModule.create(),
                    AnalyticsModule.create(),
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
        single {
            val trackers = TrackerFactory.create(this@App, BuildConfig.ANALYTICS_ENABLED)
            Analytics(trackers)
        } bind TrackerContract::class
    }

    private fun restart() {
        setupKoin()
        RootActivity
            .createIntent(this, action = RootActivity.ACTION_RESTART)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let { startActivity(it) }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}