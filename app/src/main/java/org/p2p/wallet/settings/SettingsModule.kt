package org.p2p.wallet.settings

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.model.SettingsItemMapper
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter
import org.p2p.wallet.settings.ui.recovery.RecoveryKitContract
import org.p2p.wallet.settings.ui.recovery.RecoveryKitPresenter
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinContract
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinPresenter
import org.p2p.wallet.settings.ui.settings.SettingsContract
import org.p2p.wallet.settings.ui.settings.SettingsPresenter
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics

object SettingsModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SettingsInteractor)
        factoryOf(::ThemeInteractor)

        factoryOf(::SettingsItemMapper)
        factoryOf(::SettingsPresenterAnalytics)
        factoryOf(::SettingsPresenter) bind SettingsContract.Presenter::class

        factoryOf(::RecoveryKitPresenter) bind RecoveryKitContract.Presenter::class
        factoryOf(::ResetPinPresenter) bind ResetPinContract.Presenter::class
        factoryOf(::SettingsNetworkPresenter) bind SettingsNetworkContract.Presenter::class
        factoryOf(::ResetPinPresenter) bind ResetPinContract.Presenter::class
    }
}
