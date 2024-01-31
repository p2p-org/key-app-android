package org.p2p.wallet.referral

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.referral.banner.ReferralFragmentFactory

object ReferralModule : InjectionModule {
    override fun create(): Module = module {
        factoryOf(::ReferralFragmentFactory)
    }
}
