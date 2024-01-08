package org.p2p.wallet.bridge.claim

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.bridge.claim.interactor.EthBridgeClaimInteractor
import org.p2p.wallet.bridge.claim.mapper.EthereumBundleMapper
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeInMemoryRepository
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRemoteRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.claim.ui.ClaimContract
import org.p2p.wallet.bridge.claim.ui.ClaimPresenter
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper

object ClaimModule : InjectionModule {

    override fun create() = module {
        singleOf(::EthereumClaimRemoteRepository) bind EthereumClaimRepository::class
        singleOf(::EthereumBridgeInMemoryRepository) bind EthereumBridgeLocalRepository::class
        factoryOf(::EthBridgeClaimInteractor)
        factoryOf(::ClaimUiMapper)
        factoryOf(::ClaimPresenter) bind ClaimContract.Presenter::class
        singleOf(::EthereumBundleMapper)
    }
}
