package org.p2p.wallet.striga

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.interceptor.StrigaHeaderSignatureGenerator
import org.p2p.wallet.kyc.StrigaFragmentFactory
import org.p2p.wallet.striga.di.StrigaSignupModule
import org.p2p.wallet.striga.di.StrigaWalletModule
import org.p2p.wallet.striga.kyc.StrigaKycModule

object StrigaModule : InjectionModule {

    override fun create() = module {
        includes(
            StrigaSignupModule.create(),
            StrigaKycModule.create(),
            StrigaWalletModule.create()
        )

        factoryOf(::StrigaUserIdProvider)
        factoryOf(::StrigaHeaderSignatureGenerator)
        factoryOf(::StrigaFragmentFactory)
    }
}
