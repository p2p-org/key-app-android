package org.p2p.wallet.home.ui.wallet

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.home.ui.wallet.handlers.WalletStrigaHandler
import org.p2p.wallet.home.ui.wallet.mapper.StrigaKycUiBannerMapper
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper

object WalletModule : InjectionModule {

    override fun create() = module {
        factoryOf(::WalletMapper)
        factoryOf(::StrigaKycUiBannerMapper)
        factoryOf(::WalletStrigaHandler)
        factory {
            WalletPresenter(
                usernameInteractor = get(),
                walletMapper = get(),
                tokenKeyProvider = get(),
                tokenServiceCoordinator = get(),
                strigaOnRampInteractor = get(),
                strigaOffRampInteractor = get(),
                strigaUserInteractor = get(),
                walletStrigaHandler = get(),
                strigaSignupEnabledFeatureToggle = get(),
                sellInteractor = get(),
                mainScreenAnalytics = get(),
            )
        } bind WalletContract.Presenter::class
    }
}
