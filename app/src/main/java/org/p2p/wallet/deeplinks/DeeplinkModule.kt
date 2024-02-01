package org.p2p.wallet.deeplinks

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.intercom.IntercomDeeplinkManager

object DeeplinkModule : InjectionModule {
    override fun create() = module {
        singleOf(::AppDeeplinksManager)

        factoryOf(::ReferralDeeplinkHandler)
        factoryOf(::SwapDeeplinkHandler)
        singleOf(::IntercomDeeplinkManager)
    }
}
