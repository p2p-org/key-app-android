package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class EthAddressEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "eth_address_enabled"
    override val featureDescription: String = "Is bridges enabled"
    override val defaultValue: Boolean = true
}
