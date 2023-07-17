package org.p2p.wallet.striga

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.interceptor.StrigaHeaderSignatureGenerator
import org.p2p.wallet.kyc.StrigaFragmentFactory
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.kyc.StrigaKycModule
import org.p2p.wallet.striga.offramp.StrigaOffRampModule
import org.p2p.wallet.striga.onramp.StrigaOnRampModule
import org.p2p.wallet.striga.signup.StrigaSignupModule

object StrigaModule : InjectionModule {

    override fun create() = module {
        includes(
            StrigaSignupModule.create(),
            StrigaKycModule.create(),
            StrigaOnRampModule.create(),
            StrigaOffRampModule.create()
        )

        factoryOf(::StrigaUserIdProvider)
        factoryOf(::StrigaHeaderSignatureGenerator)
        factoryOf(::StrigaFragmentFactory)
    }
}
