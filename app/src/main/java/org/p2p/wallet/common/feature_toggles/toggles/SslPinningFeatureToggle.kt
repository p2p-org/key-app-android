package org.p2p.wallet.common.feature_toggles.toggles

import org.p2p.wallet.common.feature_toggles.BooleanFeatureToggle
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesSource

class SslPinningFeatureToggle(
    valuesProvider: RemoteConfigValuesSource
) : BooleanFeatureToggle(valuesProvider) {
    override val toggleKey: String = "ssl_pinning"
    override val toggleDescription: String = "Enable SSL pinning for Wallet DNS's"
    override val defaultValue: Boolean = false
}
