package org.p2p.wallet.home.ui.wallet

import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.home.ui.wallet.handlers.WalletStrigaHandler
import org.p2p.wallet.home.ui.wallet.mapper.StrigaKycUiBannerMapper
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper
import org.p2p.wallet.striga.onramp.StrigaOnRampModule

object WalletModule : InjectionModule {

    override fun create() = module {
        factoryOf(::WalletMapper)
        factoryOf(::StrigaKycUiBannerMapper)
        factory {
            WalletStrigaHandler(
                strigaKycUiBannerMapper = get(),
                strigaUserInteractor = get(),
                strigaWalletInteractor = get(),
                strigaOnRampInteractor = get(),
                strigaWithdrawInteractor = get(),
                historyInteractor = get(),
                tokenKeyProvider = get(),
                localFeatureFlags = get(),
                appLoaderFacade = get(),
                strigaSmsInputTimer = get(named(StrigaOnRampModule.SMS_QUALIFIER)),
            )
        }
        factory {
            WalletPresenter(
                dispatchers = get(),
                usernameInteractor = get(),
                walletMapper = get(),
                tokenKeyProvider = get(),
                tokenServiceCoordinator = get(),
                walletStrigaInteractor = get(),
                walletStrigaHandler = get(),
                strigaSignupEnabledFeatureToggle = get(),
                sellInteractor = get(),
                mainScreenAnalytics = get(),
            )
        } bind WalletContract.Presenter::class
    }
}
