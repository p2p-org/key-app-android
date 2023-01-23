package org.p2p.wallet

import androidx.appcompat.app.AppCompatDelegate
import android.app.Application
import android.content.Intent
import com.jakewharton.threetenabp.AndroidThreeTen
import io.palaima.debugdrawer.timber.data.LumberYard
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.helpers.TimberCrashTree
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.utils.SolanajTimberLogger
import org.p2p.wallet.utils.getStringResourceByName
import timber.log.Timber

class App : Application() {
    private val crashLogger: CrashLogger by inject()
    private val appCreatedAction: AppCreatedAction by inject()
    private val appsFlyerService: AppsFlyerService by inject()
    private val usernameInteractor: UsernameInteractor by inject()

    override fun onCreate() {
        super.onCreate()
        setupKoin()

        setupTimber()

        setupCrashLoggingService()

        AppNotificationManager.createNotificationChannels(this)
        IntercomService.setup(app = this, apiKey = BuildConfig.intercomApiKey, appId = BuildConfig.intercomAppId)
        AndroidThreeTen.init(this)

        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()

        SolanjLogger.setLoggerImplementation(SolanajTimberLogger())

        appCreatedAction.invoke()
        appsFlyerService.install(this, BuildConfig.appsFlyerKey)
    }

    private fun setupKoin() {
        GlobalContext.stopKoin()
        startKoin {
            // crashes when using level != Level.Error
            // do NOT use other than ERROR before bumping to Koin 3.1.6
            // FIXME
            androidLogger(level = Level.ERROR)
            androidContext(this@App)
            // uncomment in PWN-4197
            // workManagerFactory inside calls WorkManager.initialize that causes IllegalStateException
            // reason: WorkManager.initialize should be called ONLY ONCE but called twice when user logouts
            // workManagerFactory()
            modules(AppModule.create(restartAction = ::restart))
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
            // for logs in debug drawer
            Timber.plant(
                LumberYard.getInstance(this)
                    .apply { cleanUp() }
                    .tree()
            )
        }
        // Always plant this tree
        // events are sent or not internally using CrashLoggingService::isLoggingEnabled flag
        Timber.plant(TimberCrashTree(crashLogger))
    }

    private fun setupCrashLoggingService() {
        crashLogger.apply {
            setCustomKey("amplitude_enabled", BuildConfig.AMPLITUDE_ENABLED)
            setCustomKey("crashlytics_enabled", BuildConfig.CRASHLYTICS_ENABLED)
            setCustomKey("verifier", getString(R.string.torusVerifier))
            setCustomKey("sub_verifier", getStringResourceByName("torusSubVerifier"))
            setCustomKey("username", usernameInteractor.getUsername()?.fullUsername.orEmpty())
        }
    }
}
