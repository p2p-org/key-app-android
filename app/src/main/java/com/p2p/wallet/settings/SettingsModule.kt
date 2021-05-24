package com.p2p.wallet.settings

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.settings.interactor.SettingsInteractor
import com.p2p.wallet.settings.ui.reset.ResetPinContract
import com.p2p.wallet.settings.ui.reset.ResetPinPresenter
import com.p2p.wallet.settings.ui.security.SecurityContract
import com.p2p.wallet.settings.ui.security.SecurityPresenter
import com.p2p.wallet.settings.ui.settings.SettingsContract
import com.p2p.wallet.settings.ui.settings.SettingsPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object SettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get()) }

        factory { SettingsPresenter(get(), get(), get()) } bind SettingsContract.Presenter::class
        factory { SecurityPresenter(get(), get()) } bind SecurityContract.Presenter::class
        factory { ResetPinPresenter(get()) } bind ResetPinContract.Presenter::class
    }
}