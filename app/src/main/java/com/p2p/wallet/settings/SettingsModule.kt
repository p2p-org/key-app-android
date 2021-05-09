package com.p2p.wallet.settings

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.settings.interactor.SettingsInteractor
import com.p2p.wallet.settings.ui.SettingsContract
import com.p2p.wallet.settings.ui.SettingsPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object SettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get()) }

        single { SettingsPresenter(get(), get(), get()) } bind SettingsContract.Presenter::class
    }
}