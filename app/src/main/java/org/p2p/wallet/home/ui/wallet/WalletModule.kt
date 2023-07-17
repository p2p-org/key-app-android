package org.p2p.wallet.home.ui.wallet

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaBannerClickHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaOnRampClickHandler
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.striga.ui.TopUpWalletContract
import org.p2p.wallet.striga.ui.TopUpWalletPresenter

object WalletModule : InjectionModule {

    override fun create() = module {
        factoryOf(::StrigaKycUiBannerMapper)
        factoryOf(::WalletPresenterMapper)
        factoryOf(::StrigaOnRampConfirmedHandler)
        factoryOf(::StrigaOnRampClickHandler)
        factoryOf(::StrigaBannerClickHandler)
        factoryOf(::WalletPresenter) bind WalletContract.Presenter::class
        factoryOf(::TopUpWalletPresenter) bind TopUpWalletContract.Presenter::class
    }
}
