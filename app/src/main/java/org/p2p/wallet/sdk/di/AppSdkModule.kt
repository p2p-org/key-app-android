package org.p2p.wallet.sdk.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.sdk.RelaySdk
import org.p2p.wallet.sdk.facade.AppSdkLogger
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.sdk.facade.mapper.SdkMethodResultMapper

object AppSdkModule : InjectionModule {

    override fun create() = module {
        singleOf(::RelaySdk)
        factoryOf(::SdkMethodResultMapper)
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
}
