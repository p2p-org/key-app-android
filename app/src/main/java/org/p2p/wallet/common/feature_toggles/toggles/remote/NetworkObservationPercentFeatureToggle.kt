package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NetworkObservationPercentFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : IntFeatureToggle(valuesProvider) {
    override val featureKey: String = "solana_negative_status_percent"
    override val featureDescription: String = "Get the Solana negative percent min allowed value"
    override val defaultValue: Int = 70
}
