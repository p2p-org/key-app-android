package org.p2p.wallet.settings

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.mapper.SettingsEntityMapper
import org.p2p.wallet.settings.mapper.SettingsItemMapper
import org.p2p.wallet.settings.ui.devices.DeviceCellMapper
import org.p2p.wallet.settings.ui.devices.DevicesContract
import org.p2p.wallet.settings.ui.devices.DevicesPresenter
import org.p2p.wallet.settings.ui.mail.SettingsEmailConfirmContract
import org.p2p.wallet.settings.ui.mail.SettingsEmailConfirmPresenter
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinContract
import org.p2p.wallet.settings.ui.resetpin.pin.ResetPinPresenter
import org.p2p.wallet.settings.ui.security.SecurityAndPrivacyContract
import org.p2p.wallet.settings.ui.security.SecurityAndPrivacyPresenter
import org.p2p.wallet.settings.ui.settings.SettingsContract
import org.p2p.wallet.settings.ui.settings.SettingsPresenter
import org.p2p.wallet.settings.ui.settings.SettingsPresenterAnalytics
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.smsinput.updatedevice.UpdateDeviceSmsInputPresenter

object SettingsModule : InjectionModule {

    override fun create() = module {
        singleOf(::SettingsInteractor)
        factoryOf(::ThemeInteractor)

        factoryOf(::SettingsItemMapper)
        factoryOf(::SettingsPresenterAnalytics)
        factory {
            SettingsPresenter(
                environmentManager = get(),
                usernameInteractor = get(),
                authLogoutInteractor = get(),
                appRestarter = get(),
                analytics = get(),
                settingsInteractor = get(),
                userTokensLocalRepository = get(),
                settingsItemMapper = get(),
                metadataInteractor = get(),
                authInteractor = get(),
                analyticsInteractor = get(),
                referralProgramEnabledFeatureToggle = get()
            )
        } bind SettingsContract.Presenter::class

        factoryOf(::SecurityAndPrivacyPresenter) bind SecurityAndPrivacyContract.Presenter::class
        factoryOf(::ResetPinPresenter) bind ResetPinContract.Presenter::class
        factoryOf(::SettingsNetworkPresenter) bind SettingsNetworkContract.Presenter::class
        factoryOf(::SettingsEmailConfirmPresenter) bind SettingsEmailConfirmContract.Presenter::class

        factoryOf(::DevicesPresenter) bind DevicesContract.Presenter::class
        factoryOf(::UpdateDeviceSmsInputPresenter) {
            bind<SmsInputContract.Presenter>()
            named(SmsInputFactory.Type.UpdateDevice.name)
        }
        factoryOf(::DeviceCellMapper)
        factoryOf(::SettingsEntityMapper)
    }
}
