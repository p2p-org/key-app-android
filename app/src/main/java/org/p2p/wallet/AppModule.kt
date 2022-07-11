package org.p2p.wallet

import android.content.res.Resources
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.impl.FirebaseCrashlyticsFacade
import org.p2p.wallet.common.crashlogging.impl.SentryFacade
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.ServiceScope

object AppModule {
    fun create(restartAction: () -> Unit) = module {
        singleOf(::AppScope)
        single<Resources> { androidContext().resources }
        singleOf(::InAppFeatureFlags)
        singleOf(::ResourcesProvider)
        singleOf(::ServiceScope)
        single { AppRestarter { restartAction.invoke() } }
        single {
            CrashLogger(
                crashLoggingFacades = listOf(
                    FirebaseCrashlyticsFacade(isFacadeEnabled = BuildConfig.CRASHLYTICS_ENABLED),
                    SentryFacade()
                ),
                tokenKeyProvider = get()
            )
        }
    }
}
