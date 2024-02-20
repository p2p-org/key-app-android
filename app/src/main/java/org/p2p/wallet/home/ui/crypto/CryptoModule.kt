package org.p2p.wallet.home.ui.crypto

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.common.di.factoryOf
import org.p2p.wallet.home.interactor.MyCryptoInteractor
import org.p2p.wallet.home.ui.crypto.handlers.BridgeClaimBundleClickHandler
import org.p2p.wallet.home.ui.crypto.mapper.MyCryptoMapper
import org.p2p.wallet.receive.list.ReceiveTokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter

object CryptoModule : InjectionModule {

    override fun create() = module {
        factoryOf(::MyCryptoInteractor)
        factoryOf(::BridgeClaimBundleClickHandler)
        factoryOf(::MyCryptoMapper)
        factoryOf(::MyCryptoPresenter) bind MyCryptoContract.Presenter::class
        factoryOf(::TokenListPresenter) bind ReceiveTokenListContract.Presenter::class
    }
}
