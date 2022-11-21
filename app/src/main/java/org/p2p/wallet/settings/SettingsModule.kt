package org.p2p.wallet.settings

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.model.SettingsItemMapper
import org.p2p.wallet.settings.ui.appearance.AppearanceContract
import org.p2p.wallet.settings.ui.appearance.AppearancePresenter
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter
import org.p2p.wallet.settings.ui.newreset.pin.NewResetPinContract
import org.p2p.wallet.settings.ui.newreset.pin.NewResetPinPresenter
import org.p2p.wallet.settings.ui.recovery.RecoveryKitContract
import org.p2p.wallet.settings.ui.recovery.RecoveryKitPresenter
import org.p2p.wallet.settings.ui.recovery.seed.SeedPhraseContract
import org.p2p.wallet.settings.ui.recovery.seed.SeedPhrasePresenter
import org.p2p.wallet.settings.ui.reset.ResetPinContract
import org.p2p.wallet.settings.ui.reset.ResetPinPresenter
import org.p2p.wallet.settings.ui.reset.seedphrase.ResetSeedPhraseContract
import org.p2p.wallet.settings.ui.reset.seedphrase.ResetSeedPhrasePresenter
import org.p2p.wallet.settings.ui.security.SecurityContract
import org.p2p.wallet.settings.ui.security.SecurityPresenter
import org.p2p.wallet.settings.ui.settings.NewSettingsContract
import org.p2p.wallet.settings.ui.settings.NewSettingsPresenter
import org.p2p.wallet.settings.ui.zerobalances.SettingsZeroBalanceContract
import org.p2p.wallet.settings.ui.zerobalances.SettingsZeroBalancesPresenter

object SettingsModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SettingsInteractor)
        factoryOf(::ThemeInteractor)
        factoryOf(::SettingsItemMapper)
        factory {
            // holy shit, TODO smth with this dependencies!
            NewSettingsPresenter(
                environmentManager = get(),
                usernameInteractor = get(),
                authLogoutInteractor = get(),
                appRestarter = get(),
                receiveAnalytics = get(),
                adminAnalytics = get(),
                browseAnalytics = get(),
                settingsInteractor = get(),
                homeLocalRepository = get(),
                secureStorage = get(),
                settingsItemMapper = get(),
                context = androidContext(),
                authInteractor = get()
            )
        } bind NewSettingsContract.Presenter::class

        factoryOf(::SecurityPresenter) bind SecurityContract.Presenter::class
        factoryOf(::RecoveryKitPresenter) bind RecoveryKitContract.Presenter::class
        factoryOf(::SeedPhrasePresenter) bind SeedPhraseContract.Presenter::class
        factoryOf(::ResetPinPresenter) bind ResetPinContract.Presenter::class
        factoryOf(::AppearancePresenter) bind AppearanceContract.Presenter::class

        factoryOf(::ResetSeedPhrasePresenter) bind ResetSeedPhraseContract.Presenter::class

        factoryOf(::SettingsNetworkPresenter) bind SettingsNetworkContract.Presenter::class
        factoryOf(::SettingsZeroBalancesPresenter) bind SettingsZeroBalanceContract.Presenter::class

        factoryOf(::NewResetPinPresenter) bind NewResetPinContract.Presenter::class
    }
}
