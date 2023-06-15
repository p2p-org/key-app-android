package org.p2p.wallet.settings

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.model.SettingsItemMapper
import org.p2p.wallet.settings.ui.devices.DeviceCellMapper
import org.p2p.wallet.settings.ui.devices.DevicesContract
import org.p2p.wallet.settings.ui.devices.DevicesPresenter
import org.p2p.wallet.settings.ui.mail.SettingsEmailConfirmContract
import org.p2p.wallet.settings.ui.mail.SettingsEmailConfirmPresenter
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter
import org.p2p.wallet.settings.ui.recovery.RecoveryKitContract
import org.p2p.wallet.settings.ui.recovery.RecoveryKitPresenter
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinContract
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinPresenter
import org.p2p.wallet.settings.ui.settings.SettingsContract
import org.p2p.wallet.settings.ui.settings.SettingsPresenter
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.smsinput.updatedevice.UpdateDeviceSmsInputPresenter

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
        factoryOf(::SettingsEmailConfirmPresenter) bind SettingsEmailConfirmContract.Presenter::class

        factoryOf(::DevicesPresenter) bind DevicesContract.Presenter::class
        factoryOf(::UpdateDeviceSmsInputPresenter) {
            bind<SmsInputContract.Presenter>()
            named(SmsInputFactory.Type.UpdateDevice.name)
        }
        factoryOf(::DeviceCellMapper)
    }
}
