package org.p2p.wallet.referral

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.referral.repository.ReferralApi
import org.p2p.wallet.referral.repository.ReferralRemoteRepository
import org.p2p.wallet.referral.repository.ReferralRepository

object ReferralModule : InjectionModule {
    override fun create() = module {
        factoryOf(::ReferralRemoteRepository) bind ReferralRepository::class
        single<ReferralApi> {
            getRetrofit(
                baseUrl = "https://send-service.key.app/",
                tag = "ReferralApi",
                interceptor = null
            ).create()
        }
    }
}
