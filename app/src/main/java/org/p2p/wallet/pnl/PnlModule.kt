package org.p2p.wallet.pnl

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.infrastructure.StorageModule
import org.p2p.wallet.pnl.interactor.PnlInteractor
import org.p2p.wallet.pnl.repository.PnlMockRepository
import org.p2p.wallet.pnl.repository.PnlRepository
import org.p2p.wallet.pnl.storage.PnlStorage
import org.p2p.wallet.pnl.storage.PnlStorageContract
import org.p2p.wallet.pnl.ui.PnlUiMapper

/**
 * PNL - Profit`n`Loss
 */
object PnlModule : InjectionModule {
    private const val RETROFIT_QUALIFIER = "PNL_SERVICE_RETROFIT_QUALIFIER"
    override fun create(): Module = module {
        single(named(RETROFIT_QUALIFIER)) {
            // todo: change to real url when backend is ready
            val rpcApiUrl = "https://pnl-something.com/"
            getRetrofit(
                baseUrl = rpcApiUrl,
                tag = "PNLServiceRpc",
                interceptor = null
            )
        }

        single {
            PnlStorage(
                sharedPreferences = get(named(StorageModule.PREFS_PNL)),
                gson = get()
            )
        } bind PnlStorageContract::class

        /* todo: backend is not ready yet
        single {
            val api = get<Retrofit>(named(RETROFIT_QUALIFIER)).create(PnlServiceApi::class.java)
            PnlRemoteRepository(
                api = api,
                storage = get()
            )
        } bind PnlRepository::class
        */
        singleOf(::PnlMockRepository) bind PnlRepository::class

        factoryOf(::PnlUiMapper)
        factoryOf(::PnlInteractor)
    }
}
