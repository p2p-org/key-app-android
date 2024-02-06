package org.p2p.wallet.referral

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.referral.banner.ReferralFragmentFactory
import org.p2p.wallet.referral.repository.ReferralRemoteRepository
import org.p2p.wallet.referral.repository.ReferralRepository

object ReferralModule : InjectionModule {
    override fun create() = module {
        factoryOf(::ReferralFragmentFactory)
        factoryOf(::ReferralRemoteRepository) bind ReferralRepository::class
    }
}
