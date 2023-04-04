package org.p2p.wallet.bridge.claim

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.repository.EthereumClaimLocalRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRemoteRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.claim.ui.ClaimContract
import org.p2p.wallet.bridge.claim.ui.ClaimPresenter
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.common.di.InjectionModule

object ClaimModule : InjectionModule {

    override fun create() = module {
        singleOf(::EthereumClaimRemoteRepository) bind EthereumClaimRepository::class
        factoryOf(::ClaimInteractor)
        factoryOf(::ClaimUiMapper)
        factoryOf(::ClaimPresenter) bind ClaimContract.Presenter::class
        singleOf(::EthereumClaimLocalRepository)
    }
}
