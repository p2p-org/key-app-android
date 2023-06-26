package org.p2p.wallet.common.feature_toggles.toggles.remote

import java.util.concurrent.TimeUnit
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

private const val DEFAULT_INTERVAL_IN_SECONDS = 20L

class SwapRoutesRefreshFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : LongFeatureToggle(valuesProvider) {
    override val featureKey: String = "swap_route_refresh"
    override val featureDescription: String = "The interval for refreshing routes"
    override val defaultValue: Long = DEFAULT_INTERVAL_IN_SECONDS

    val durationInMilliseconds: Long = TimeUnit.SECONDS.toMillis(value)
}
