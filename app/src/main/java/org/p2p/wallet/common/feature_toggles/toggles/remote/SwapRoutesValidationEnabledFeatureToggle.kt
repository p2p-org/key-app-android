package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SwapRoutesValidationEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "swap_transaction_simulation_enabled"
    override val featureDescription: String = "Is swap routes validation enabled"
    override val defaultValue: Boolean = false
}
