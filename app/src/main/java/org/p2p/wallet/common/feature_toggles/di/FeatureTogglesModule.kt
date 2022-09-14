package org.p2p.wallet.common.feature_toggles.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.FeatureTogglesValuesSource
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import org.p2p.wallet.common.feature_toggles.toggles.remote.BuyWithTransferFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SettingsNetworkListFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SslPinningFeatureToggle

object FeatureTogglesModule : InjectionModule {
    override fun create() = module {
        single(createdAtStart = true) { AppFirebaseRemoteConfig() }
        singleOf(::LocalFirebaseRemoteConfig)

        singleOf(::FeatureTogglesValuesSource) bind RemoteConfigValuesProvider::class

        factory {
            listOf(
                get<SslPinningFeatureToggle>(),
                get<NewBuyFeatureToggle>(),
                get<BuyWithTransferFeatureToggle>(),
                get<SettingsNetworkListFeatureToggle>()
            )
        }

        factoryOf(::SslPinningFeatureToggle)
        factoryOf(::NewBuyFeatureToggle)
        factoryOf(::BuyWithTransferFeatureToggle)
        factoryOf(::SettingsNetworkListFeatureToggle)
    }
}
