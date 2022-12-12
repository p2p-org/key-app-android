package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SellEnabledFeatureToggle(valuesProvider: RemoteConfigValuesProvider) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "keyapp_sell_scenario_enabled"
    override val featureDescription: String = "Is Moonpay sell flow enabled"
    override val defaultValue: Boolean = true
}
