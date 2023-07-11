package org.p2p.wallet.common.feature_toggles.di

import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.FeatureTogglesValuesSource
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationDebounceFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationFrequencyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationPercentFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameSkipEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SendViaLinkFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SettingsNetworkListFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SocketSubscriptionsFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SslPinningFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesRefreshFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesValidationEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.TokenMetadataUpdateFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle

object FeatureTogglesModule : InjectionModule {
    override fun create() = module {
        singleOf(::AppFirebaseRemoteConfig) { createdAtStart() }
        singleOf(::LocalFirebaseRemoteConfig)

        singleOf(::FeatureTogglesValuesSource) bind RemoteConfigValuesProvider::class

        factory {
            setOf(
                get<SslPinningFeatureToggle>(),
                get<NewBuyFeatureToggle>(),
                get<SettingsNetworkListFeatureToggle>(),
                get<NetworkObservationFeatureToggle>(),
                get<SendViaLinkFeatureToggle>(),
                get<UsernameDomainFeatureToggle>(),
                get<RegisterUsernameEnabledFeatureToggle>(),
                get<RegisterUsernameSkipEnabledFeatureToggle>(),
                get<SellEnabledFeatureToggle>(),
                get<EthAddressEnabledFeatureToggle>(),
                get<SocketSubscriptionsFeatureToggle>(),
                get<SwapRoutesValidationEnabledFeatureToggle>(),
                get<StrigaSignupEnabledFeatureToggle>(),
                get<SwapRoutesRefreshFeatureToggle>(),
            ).toList()
        }

        factoryOf(::SslPinningFeatureToggle)
        factoryOf(::NewBuyFeatureToggle)
        factoryOf(::SettingsNetworkListFeatureToggle)
        factoryOf(::NetworkObservationFeatureToggle)
        factoryOf(::SendViaLinkFeatureToggle)
        factoryOf(::TokenMetadataUpdateFeatureToggle)
        factoryOf(::NetworkObservationPercentFeatureToggle)
        factoryOf(::NetworkObservationFrequencyFeatureToggle)
        factoryOf(::NetworkObservationDebounceFeatureToggle)
        factoryOf(::UsernameDomainFeatureToggle)
        factoryOf(::RegisterUsernameEnabledFeatureToggle)
        factoryOf(::RegisterUsernameSkipEnabledFeatureToggle)
        factoryOf(::SellEnabledFeatureToggle)
        factoryOf(::EthAddressEnabledFeatureToggle)
        factoryOf(::SwapRoutesRefreshFeatureToggle)
        factoryOf(::SocketSubscriptionsFeatureToggle)
        factoryOf(::SwapRoutesValidationEnabledFeatureToggle)
        factoryOf(::StrigaSignupEnabledFeatureToggle)
    }
}
