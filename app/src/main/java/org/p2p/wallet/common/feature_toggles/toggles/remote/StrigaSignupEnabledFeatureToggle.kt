package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class StrigaSignupEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "striga_enabled"
    override val featureDescription: String = "Is striga sign up via KYC enabled"
    override val defaultValue: Boolean = false
    override val value: Boolean
        get() = false
}
