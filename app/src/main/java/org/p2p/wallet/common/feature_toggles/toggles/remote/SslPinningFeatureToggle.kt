package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SslPinningFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "ssl_pinning"
    override val featureDescription: String = "Enable SSL pinning for Wallet DNS's"
    override val defaultValue: Boolean = false
    override val value: Boolean
        get() = super.value && BuildConfig.SSL_PINNING_ENABLED
}
