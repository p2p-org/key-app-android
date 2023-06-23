package org.p2p.wallet.common.feature_toggles.toggles.remote

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

private const val DEFAULT_INTERVAL_IN_MILLIS = 20_000L

class SwapRoutesRefreshFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : LongFeatureToggle(valuesProvider) {
    override val featureKey: String = "swap_route_refresh"
    override val featureDescription: String = "The interval for refreshing routes"
    override val defaultValue: Long = DEFAULT_INTERVAL_IN_MILLIS

    val duration: Duration = value.toDuration(DurationUnit.MILLISECONDS)
}
