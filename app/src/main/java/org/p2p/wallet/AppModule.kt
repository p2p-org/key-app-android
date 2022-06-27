package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.p2p.wallet.common.InAppFeatureFlags
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
        single { InAppFeatureFlags(get()) }
        single { AppRestarter { restartAction.invoke() } }
        single<CrashLoggingService> { GoogleFirebaseCrashlytics(get()) }
    }
}
