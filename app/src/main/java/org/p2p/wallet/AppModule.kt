package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.crashlytics.CrashLoggingService
import org.p2p.wallet.common.crashlytics.impl.GoogleFirebaseCrashlytics
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.ServiceScope

object AppModule {
    fun create(restartAction: () -> Unit) = module {
        single { AppScope() }
        single<Resources> { androidContext().resources }
        single { ResourcesProvider(get()) }
        single { ServiceScope() }
        single { AppFeatureFlags(get()) }
        single { AppRestarter { restartAction.invoke() } }
        single<CrashLoggingService> { GoogleFirebaseCrashlytics(get()) }
    }
}
