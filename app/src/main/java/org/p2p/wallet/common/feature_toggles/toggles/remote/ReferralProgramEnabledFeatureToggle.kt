package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class ReferralProgramEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "referral_program_enabled"
    override val featureDescription: String = "Enables referral program functions"
    override val defaultValue: Boolean = false

    override val value: Boolean
        get() = true
}
