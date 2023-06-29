package org.p2p.wallet.sdk.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.sdk.RelaySdk
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.AppSdkLogger
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.sdk.facade.mapper.SdkMethodResultMapper

object AppSdkModule : InjectionModule {

    override fun create() = module {
        singleOf(::SolendSdk)
        singleOf(::RelaySdk)
        factoryOf(::SdkMethodResultMapper)
        initSolendSdkLayer()
        initRelaySdkLayer()
    }

    private fun Module.initRelaySdkLayer() {
        single {
            RelaySdkFacade(
                relaySdk = get(),
                logger = AppSdkLogger(),
                methodResultMapper = get(),
                dispatchers = get()
            )
        }
    }

    private fun Module.initSolendSdkLayer() {
        single {
            SolendSdkFacade(
                solendSdk = get(),
                networkEnvironmentManager = get(),
                methodResultMapper = get(),
                logger = AppSdkLogger(),
                gson = get(),
                dispatchers = get()
            )
        }
    }
}
