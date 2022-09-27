package solendsdkfacade.di

import P2pSdk
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import solendsdkfacade.SolendMethodResultHandler
import solendsdkfacade.SolendSdkFacade
import solendsdkfacade.mapper.SolendMethodResultMapper
import solendsdkfacade.model.SolendEnvironment
import solendsdkfacade.model.SolendMethodResultError
import solendsdkfacade.model.SolendMethodResultSuccess

object P2PSdkModule : InjectionModule {

    override fun create() = module {
        singleOf(::P2pSdk)

        initSolendDeps()
    }

    private fun Module.initSolendDeps() {
        single {
            SolendSdkFacade(
                p2pSdk = get(),
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
