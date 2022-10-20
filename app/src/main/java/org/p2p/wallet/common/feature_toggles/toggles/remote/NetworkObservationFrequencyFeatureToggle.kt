package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import org.p2p.wallet.solana.model.NetworkStatusFrequency

class NetworkObservationFrequencyFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : StringFeatureToggle(valuesProvider) {
    override val featureKey: String = "solana_negative_status_time_frequency"
    override val featureDescription: String = "The frequency of error shown to the user in case of the network error"
    override val defaultValue: String = "Once"

    val frequency: NetworkStatusFrequency
        get() = NetworkStatusFrequency.parse(value)
}
