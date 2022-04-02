package org.p2p.wallet

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.di.AppScope

object AppModule {
    fun create(restartAction: () -> Unit): Module = module {
        single { AppScope() }
        single { AppFeatureFlags(get()) }
        single { AppRestarter { restartAction.invoke() } } bind AppRestarter::class
    }
}
