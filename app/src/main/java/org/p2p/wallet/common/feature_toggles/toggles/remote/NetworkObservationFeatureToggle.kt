package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NetworkObservationFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "solana_negative_status_enabled"
    override val featureDescription: String = "Enable Solana network negative status observation feature"
    override val defaultValue: Boolean = false
}
