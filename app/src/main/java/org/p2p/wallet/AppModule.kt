package org.p2p.wallet

import android.app.Application
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.TrackerContract
import org.p2p.wallet.common.analytics.TrackerFactory
import org.p2p.wallet.common.crashlytics.CrashLoggingService
import org.p2p.wallet.common.crashlytics.impl.GoogleFirebaseCrashlytics
import org.p2p.wallet.common.di.AppScope

object AppModule {
    fun create(
        application: Application,
        restartAction: () -> Unit
    ): Module {
        return module {
            single { AppScope() }
            single { AppRestarter { restartAction() } } bind AppRestarter::class
            single {
                val trackers = TrackerFactory.create(application, BuildConfig.ANALYTICS_ENABLED)
                Analytics(trackers)
            } bind TrackerContract::class
            single { GoogleFirebaseCrashlytics(get()) } bind CrashLoggingService::class
        }
    }
}