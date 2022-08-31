package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NewBuyFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "buy_feature"
    override val featureDescription: String = "Enable new buy feature"
    override val defaultValue: Boolean = true
}
