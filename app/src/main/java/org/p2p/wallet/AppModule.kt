package org.p2p.wallet

import android.app.Application
import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.TrackersFactory
import org.p2p.wallet.common.crashlytics.CrashLoggingService
import org.p2p.wallet.common.crashlytics.impl.GoogleFirebaseCrashlytics
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.ServiceScope

object AppModule {
    fun create(
        application: Application,
        restartAction: () -> Unit
    ): Module {
        return module {
            single { AppScope() }
            single { androidContext().resources } bind Resources::class
            single { androidContext().theme } bind Resources.Theme::class
            single { ServiceScope() }
            single { AppFeatureFlags(get()) }
            single { AppRestarter { restartAction() } } bind AppRestarter::class
            single { Analytics(TrackersFactory.create(application)) }
            single { GoogleFirebaseCrashlytics(get()) } bind CrashLoggingService::class
        }
    }
}
