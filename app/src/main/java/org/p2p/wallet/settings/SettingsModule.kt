package org.p2p.wallet.settings

import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.ui.appearance.AppearanceContract
import org.p2p.wallet.settings.ui.appearance.AppearancePresenter
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter
import org.p2p.wallet.settings.ui.reset.ResetPinContract
import org.p2p.wallet.settings.ui.reset.ResetPinPresenter
import org.p2p.wallet.settings.ui.reset.seedphrase.ResetSeedPhraseContract
import org.p2p.wallet.settings.ui.reset.seedphrase.ResetSeedPhrasePresenter
import org.p2p.wallet.settings.ui.security.SecurityContract
import org.p2p.wallet.settings.ui.security.SecurityPresenter
import org.p2p.wallet.settings.ui.settings.SettingsContract
import org.p2p.wallet.settings.ui.settings.SettingsPresenter
import org.p2p.wallet.settings.ui.zerobalances.SettingsZeroBalanceContract
import org.p2p.wallet.settings.ui.zerobalances.SettingsZeroBalancesPresenter

object SettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get(), get()) }
        factory { ThemeInteractor(get()) }
        factory {
            SettingsPresenter(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        } bind SettingsContract.Presenter::class
        factory { SecurityPresenter(get(), get()) } bind SecurityContract.Presenter::class
        factory { ResetPinPresenter(get(), get(), get()) } bind ResetPinContract.Presenter::class
        factory { AppearancePresenter(get()) } bind AppearanceContract.Presenter::class
        factory { ResetSeedPhrasePresenter(get(), get()) } bind ResetSeedPhraseContract.Presenter::class
        factory {
            SettingsNetworkPresenter(
                context = get(),
                appFeatureFlags = get(),
                mainLocalRepository = get(),
                environmentManager = get(),
                analytics = get()
            )
        } bind SettingsNetworkContract.Presenter::class
        factory { SettingsZeroBalancesPresenter(get()) } bind SettingsZeroBalanceContract.Presenter::class
    }
}
