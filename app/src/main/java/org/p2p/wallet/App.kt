package org.p2p.wallet

import androidx.appcompat.app.AppCompatDelegate
import android.app.Application
import android.content.Intent
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.crashlytics.CrashLoggingService
import org.p2p.wallet.common.crashlytics.TimberCrashTree
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
import org.p2p.wallet.utils.SolanajTimberLogger
import timber.log.Timber

class App : Application() {

    private val crashLoggingService: CrashLoggingService by inject()

    override fun onCreate() {
        super.onCreate()
        setupKoin()

        setupTimber()

        crashLoggingService.isLoggingEnabled = BuildConfig.CRASHLYTICS_ENABLED
        crashLoggingService.setCustomKey(BuildConfig.TASK_NUMBER, "")

        AppNotificationManager.createNotificationChannels(this)
        IntercomService.setup(this, BuildConfig.intercomApiKey, BuildConfig.intercomAppId)
        AndroidThreeTen.init(this)
        DebugDrawer.init(this)

        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()

        SolanjLogger.setLoggerImplementation(SolanajTimberLogger())
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
                    AppModule.create(application = this@App, restartAction = ::restart)
                )
            )
        }
    }

    private fun restart() {
        setupKoin()

        RootActivity
            .createIntent(this, action = RootActivity.ACTION_RESTART)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let { startActivity(it) }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Always plant this tree
        // events are sent or not internally using CrashLoggingService::isLoggingEnabled flag
        Timber.plant(TimberCrashTree(crashLoggingService))
    }
}
