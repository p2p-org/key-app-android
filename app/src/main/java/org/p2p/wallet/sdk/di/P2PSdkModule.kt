package org.p2p.wallet.sdk.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.SolendSdkLogger
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendEnvironment

object P2PSdkModule : InjectionModule {

    override fun create() = module {
        singleOf(::SolendSdk)

        initSolendSdkLayer()
    }

    private fun Module.initSolendSdkLayer() {
        single {
            SolendSdkFacade(
                solendSdk = get(),
                solendEnvironment = SolendEnvironment.DEVNET,
                networkEnvironmentManager = get(),
                methodResultMapper = get(),
                logger = SolendSdkLogger(),
                gson = get(),
                dispatchers = get()
            )
        }
        factoryOf(::SolendMethodResultMapper)
    }
}
