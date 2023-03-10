package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NewSwapEnabledFeatureToggle(valuesProvider: RemoteConfigValuesProvider) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "keyapp_swap_scenario_enabled"
    override val featureDescription: String = "Is new swap flow enabled"
    override val defaultValue: Boolean = false
}
