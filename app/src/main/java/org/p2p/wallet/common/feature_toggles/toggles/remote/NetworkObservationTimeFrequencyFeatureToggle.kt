package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import java.util.concurrent.TimeUnit

class NetworkObservationTimeFrequencyFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : IntFeatureToggle(valuesProvider) {
    override val featureKey: String = "solana_negative_status_time_frequency"
    override val featureDescription: String = "The frequency of error shown to the user in case of the network error"
    override val defaultValue: Int = 10

    val secondsInMillis: Long
        get() = TimeUnit.SECONDS.toMillis(value.toLong())
}
