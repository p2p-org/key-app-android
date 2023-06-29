package org.p2p.core.crashlytics

import org.koin.core.module.Module
import org.koin.dsl.module
import org.p2p.core.BuildConfig
import org.p2p.core.common.di.InjectionModule


object CrashLoggerModule: InjectionModule {

    override fun create(): Module = module {
        single {
            CrashLogger(crashLoggingFacades = getActiveCrashLoggingFacades())
        }
    }
}
private fun getActiveCrashLoggingFacades(): List<CrashLoggingFacade> = buildList {
    if (BuildConfig.CRASHLYTICS_ENABLED) {
        add(FirebaseCrashlyticsFacade())
    }
    if (BuildConfig.SENTRY_ENABLED) {
        add(SentryFacade())
    }
}
