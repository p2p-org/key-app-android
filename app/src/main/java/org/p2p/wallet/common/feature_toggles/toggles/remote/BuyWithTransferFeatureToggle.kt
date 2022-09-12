package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class BuyWithTransferFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "buy_bank_transfer_enabled"
    override val featureDescription: String = "Enable buy with bank transfer feature"
    override val defaultValue: Boolean = false
}
