package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class RegisterUsernameEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "android_onboarding_username_enabled"
    override val featureDescription: String = "Enable username reserve feature"
    override val defaultValue: Boolean = false
}
