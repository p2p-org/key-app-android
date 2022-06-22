package org.p2p.wallet.common.feature_toggles.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.FeatureTogglesValuesSourceChooser
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesSource
import org.p2p.wallet.common.feature_toggles.toggles.SettingsNetworkListFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.SslPinningFeatureToggle

object FeatureTogglesModule : InjectionModule {
    override fun create() = module {
        singleOf(::AppFirebaseRemoteConfig)
        singleOf(::LocalFirebaseRemoteConfig)

        singleOf(::FeatureTogglesValuesSourceChooser) bind RemoteConfigValuesSource::class

        factory {
            listOf(
                get<SslPinningFeatureToggle>(),
                get<SettingsNetworkListFeatureToggle>()
            )
        }

        factoryOf(::SslPinningFeatureToggle)
        factoryOf(::SettingsNetworkListFeatureToggle)
    }
}
