package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SslPinningFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "ssl_pinning"
    override val featureDescription: String = "Enable SSL pinning for Wallet DNS's"
    override val defaultValue: Boolean = false

    // TODO: turn back when new certificate will be ready - https://p2pvalidator.atlassian.net/browse/PWN-3211
    override val value: Boolean
        get() = false
}
