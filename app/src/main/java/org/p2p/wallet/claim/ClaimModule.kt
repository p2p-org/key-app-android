package org.p2p.wallet.claim

import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.wallet.claim.api.BridgeApi
import org.p2p.wallet.claim.interactor.ClaimInteractor
import org.p2p.wallet.claim.repository.EthereumClaimRemoteRepository
import org.p2p.wallet.claim.repository.EthereumClaimRepository
import org.p2p.wallet.claim.ui.ClaimContract
import org.p2p.wallet.claim.ui.ClaimPresenter
import org.p2p.wallet.common.di.InjectionModule

object ClaimModule : InjectionModule {

    override fun create() = module {
        single {
            val api = get<Retrofit>(named(EthereumModule.BRIDGES_SERVICE_RETROFIT_QUALIFIER))
                .create(BridgeApi::class.java)
            EthereumClaimRemoteRepository(api)
        } bind EthereumClaimRepository::class
        factoryOf(::ClaimInteractor)
        factoryOf(::ClaimPresenter) bind ClaimContract.Presenter::class
    }
}
