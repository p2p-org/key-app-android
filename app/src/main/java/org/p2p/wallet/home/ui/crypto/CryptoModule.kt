package org.p2p.wallet.home.ui.crypto

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.home.model.CryptoPresenterMapper
import org.p2p.wallet.home.ui.crypto.handlers.ClaimHandler
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter

object CryptoModule : InjectionModule {

    override fun create() = module {
        factory {
            MyCryptoInteractor(
                userInteractor = get(),
                settingsInteractor = get(),
                ethereumInteractor = get(),
            )
        }
        factoryOf(::ClaimHandler)
        factoryOf(::CryptoPresenterMapper)
        factoryOf(::MyCryptoPresenter) bind MyCryptoContract.Presenter::class
        factoryOf(::TokenListPresenter) bind TokenListContract.Presenter::class
    }
}
