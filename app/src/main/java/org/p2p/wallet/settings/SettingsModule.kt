package org.p2p.wallet.settings

import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.repository.SettingsInMemoryRepository
import org.p2p.wallet.settings.repository.SettingsLocalRepository
import org.p2p.wallet.settings.ui.appearance.AppearanceContract
import org.p2p.wallet.settings.ui.appearance.AppearancePresenter
import org.p2p.wallet.settings.ui.network.NetworkContract
import org.p2p.wallet.settings.ui.network.NetworkPresenter
import org.p2p.wallet.settings.ui.reset.ResetPinContract
import org.p2p.wallet.settings.ui.reset.ResetPinPresenter
import org.p2p.wallet.settings.ui.security.SecurityContract
import org.p2p.wallet.settings.ui.security.SecurityPresenter
import org.p2p.wallet.settings.ui.settings.SettingsContract
import org.p2p.wallet.settings.ui.settings.SettingsPresenter

object SettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get(),get()) }
        factory { ThemeInteractor(get()) }
        single { SettingsInMemoryRepository() } bind SettingsLocalRepository::class
        factory { SettingsPresenter(get(), get(),get()) } bind SettingsContract.Presenter::class
        factory { SecurityPresenter(get(), get()) } bind SecurityContract.Presenter::class
        factory { ResetPinPresenter(get()) } bind ResetPinContract.Presenter::class
        factory { NetworkPresenter(get(), get(), get()) } bind NetworkContract.Presenter::class
        factory { AppearancePresenter(get()) } bind AppearanceContract.Presenter::class
    }
}