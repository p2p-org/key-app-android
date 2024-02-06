package org.p2p.wallet.pnl

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.pnl.api.PnlServiceApi
import org.p2p.wallet.pnl.interactor.PnlDataObserver
import org.p2p.wallet.pnl.repository.PnlRemoteRepository
import org.p2p.wallet.pnl.repository.PnlRepository
import org.p2p.wallet.pnl.ui.PnlUiMapper

/**
 * PNL - Profit`n`Loss
 */
object PnlModule : InjectionModule {
    private const val RETROFIT_QUALIFIER = "PNL_SERVICE_RETROFIT_QUALIFIER"
    override fun create(): Module = module {
        factory(named(RETROFIT_QUALIFIER)) {
            val rpcApiUrl = "https://pnl.key.app/"
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "PNLServiceRpc",
                interceptor = null
            )
        }

        factory {
            val api = get<Retrofit>(named(RETROFIT_QUALIFIER)).create(PnlServiceApi::class.java)
            PnlRemoteRepository(
                api = api
            )
        } bind PnlRepository::class

        factoryOf(::PnlUiMapper)
        singleOf(::PnlDataObserver)
    }
}
