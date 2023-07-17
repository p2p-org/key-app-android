package org.p2p.wallet.home.ui.wallet

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaBannerClickHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaOnRampClickHandler
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycUiBannerMapper

object WalletModule : InjectionModule {

    override fun create() = module {
        factoryOf(::WalletMapper)
        factoryOf(::StrigaKycUiBannerMapper)
        factoryOf(::StrigaOnRampConfirmedHandler)
        factoryOf(::StrigaOnRampClickHandler)
        factoryOf(::StrigaBannerClickHandler)
        factoryOf(::WalletPresenter) bind WalletContract.Presenter::class
    }
}
