package org.p2p.wallet.debug

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesContract
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesPresenter
import org.p2p.wallet.debug.pushnotifications.PushNotificationsContract
import org.p2p.wallet.debug.pushnotifications.PushNotificationsPresenter
import org.p2p.wallet.debug.settings.DebugSettingsContract
import org.p2p.wallet.debug.settings.DebugSettingsPresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.settings.ui.network.SettingsNetworkContract
import org.p2p.wallet.settings.ui.network.SettingsNetworkPresenter

object DebugSettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get(), get()) }
        factory { ThemeInteractor(get()) }
        factory {
            DebugSettingsPresenter(
                get(),
                get(),
                get()
            )
        } bind DebugSettingsContract.Presenter::class
        factory {
            SettingsNetworkPresenter(
                context = get(),
                inAppFeatureFlags = get(),
                mainLocalRepository = get(),
                environmentManager = get(),
                analytics = get()
            )
        } bind SettingsNetworkContract.Presenter::class
        factoryOf(::FeatureTogglesPresenter) bind FeatureTogglesContract.Presenter::class
        factoryOf(::PushNotificationsPresenter) bind PushNotificationsContract.Presenter::class
    }
}
