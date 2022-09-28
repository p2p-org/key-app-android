package org.p2p.wallet.sdk.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.SolendMethodResultHandler
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendEnvironment
import org.p2p.wallet.sdk.facade.model.SolendMethodResultError
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess

object P2PSdkModule : InjectionModule {

    override fun create() = module {
        singleOf(::SolendSdk)

        initSolendDeps()
    }

    private fun Module.initSolendDeps() {
        single {
            SolendSdkFacade(
                solendSdk = get(),
                solendEnvironment = SolendEnvironment.DEVNET,
                networkEnvironmentManager = get(),
                methodResultHandler = object : SolendMethodResultHandler {
                    override fun handleResultError(error: SolendMethodResultError) = Unit
                    override fun handleResultSuccess(result: SolendMethodResultSuccess) = Unit
                },
                methodResultMapper = get(),
                gson = get(),
                dispatchers = get()
            )
        }
        factoryOf(::SolendMethodResultMapper)
    }
}
