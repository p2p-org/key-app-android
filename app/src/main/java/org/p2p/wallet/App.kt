package org.p2p.wallet

import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import androidx.work.WorkManager
import android.app.Application
import android.content.Intent
import android.util.Log.DEBUG
import android.util.Log.ERROR
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import io.palaima.debugdrawer.timber.data.LumberYard
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.core.crashlytics.helpers.TimberCrashTree
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.common.analytics.AppPublicKeyObserver
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.lokalise.LokaliseService
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.utils.CrashLoggerInitializer
import org.p2p.wallet.utils.SolanajTimberLogger
import org.p2p.core.BuildConfig as CoreBuildConfig
import org.p2p.wallet.BuildConfig as AppBuildConfig

@Suppress("KotlinConstantConditions")
val isLocalDebugBuild: Boolean
    get() = org.p2p.wallet.BuildConfig.DEBUG && org.p2p.wallet.BuildConfig.BUILD_TYPE == "debug"

class App : Application(), Configuration.Provider {
    private val crashLoggerInitializer: CrashLoggerInitializer by inject()
    private val crashLogger: CrashLogger by inject()
    private val appCreatedAction: AppCreatedAction by inject()
    private val appsFlyerService: AppsFlyerService by inject()
    private val analyticsPublicKeyObserver: AppPublicKeyObserver by inject()

    override fun onCreate() {
        super.onCreate()
        setupKoin()

        setupTimber()

        setupCrashLoggingService()
        analyticsPublicKeyObserver.startObserving()

        IntercomService.setup(
            app = this,
            apiKey = CoreBuildConfig.intercomApiKey,
            appId = CoreBuildConfig.intercomAppId
        )
        AndroidThreeTen.init(this)

        GlobalContext.get().get<ThemeInteractor>().applyCurrentNightMode()

        SolanjLogger.setLoggerImplementation(SolanajTimberLogger())

        appCreatedAction.invoke()
        appsFlyerService.install(
            application = this,
            devKey = CoreBuildConfig.appsFlyerKey
        )
        LokaliseService.setup(
            context = this,
            lokaliseToken = CoreBuildConfig.lokaliseKey,
            projectId = CoreBuildConfig.lokaliseAppId
        )
        setupWorkManager()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        val builder = Configuration.Builder()

        if (AppBuildConfig.DEBUG) {
            builder.setMinimumLoggingLevel(DEBUG)
        } else {
            builder.setMinimumLoggingLevel(ERROR)
        }

        return builder.build()
    }

    private fun setupKoin() {
        GlobalContext.stopKoin()
        startKoin {
            // crashes when using level != Level.Error
            // do NOT use other than ERROR before bumping to Koin 3.1.6
            // FIXME
            androidLogger(level = Level.INFO)
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

    private fun setupWorkManager() {
        WorkManager.initialize(this, workManagerConfiguration)
    }

    private fun setupTimber() {
        if (AppBuildConfig.DEBUG) {
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
        FirebaseCrashlytics
            .getInstance()
            .setCrashlyticsCollectionEnabled(CoreBuildConfig.CRASHLYTICS_ENABLED)

        crashLoggerInitializer.init()
    }
}
