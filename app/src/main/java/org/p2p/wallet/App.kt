package org.p2p.wallet

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.analytics.AnalyticsModule
import org.p2p.wallet.common.crashlytics.CrashLoggingService
import org.p2p.wallet.common.crashlytics.TimberCrashTree
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.debug.DebugSettingsModule
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.feerelayer.FeeRelayerModule
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.qr.ScanQrModule
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
import org.p2p.wallet.user.repository.prices.di.TokenPricesModule
import org.p2p.wallet.utils.SolanajTimberLogger
import timber.log.Timber

class App : Application() {

    private val crashLoggingService: CrashLoggingService by inject()
    private val appScope: AppScope by inject()
    private val pushTokenRepository: PushTokenRepository by inject()

    override fun onCreate() {
        super.onCreate()
        setupKoin()

        setupTimber()

        setupCrashLoggingService()

        AppNotificationManager.createNotificationChannels(this)
        IntercomService.setup(this, BuildConfig.intercomApiKey, BuildConfig.intercomAppId)
        AndroidThreeTen.init(this)
        DebugDrawer.init(this)

        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()

        SolanjLogger.setLoggerImplementation(SolanajTimberLogger())

        if (BuildConfig.DEBUG) {
            logFirebaseDevicePushToken()
        }
    }

    private fun setupKoin() {
        GlobalContext.stopKoin()
        startKoin {
            // crashes when using level != Level.Error
            // do NOT use other than ERROR before bumping to Koin 3.1.6
            // FIXME
            androidLogger(level = Level.ERROR)
            androidContext(this@App)
            modules(
                listOf(
                    // core modules
                    NetworkModule.create(),
                    RpcModule.create(),
                    FeeRelayerModule.create(),
                    InfrastructureModule.create(),
                    TransactionModule.create(),
                    AnalyticsModule.create(),
                    AppModule.create(restartAction = ::restart),

                    // feature screens
                    AuthModule.create(),
                    RootModule.create(),
                    PushNotificationsModule.create(),
                    BackupModule.create(),
                    UserModule.create(),
                    TokenPricesModule.create(),
                    HomeModule.create(),
                    RenBtcModule.create(),
                    ScanQrModule.create(),
                    HistoryModule.create(),
                    SettingsModule.create(),
                    DebugSettingsModule.create(),
                    SwapModule.create(),
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

    private fun logFirebaseDevicePushToken() {
        appScope.launch {
            kotlin.runCatching { pushTokenRepository.getPushToken().value }
                .onSuccess { Timber.tag("App:device_token").d(it) }
                .onFailure { Timber.e(it) }
        }
    }

    private fun setupCrashLoggingService() {
        crashLoggingService.apply {
            isLoggingEnabled = BuildConfig.CRASHLYTICS_ENABLED
            setCustomKey(BuildConfig.TASK_NUMBER, "")
        }
    }
}
