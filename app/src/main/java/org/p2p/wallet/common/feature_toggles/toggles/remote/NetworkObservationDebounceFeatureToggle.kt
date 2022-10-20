package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NetworkObservationDebounceFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : IntFeatureToggle(valuesProvider) {
    override val featureKey: String = "solana_negative_status_time_frequency"
    override val featureDescription: String = "The debounce between network requests"
    override val defaultValue: Int = 10
}
