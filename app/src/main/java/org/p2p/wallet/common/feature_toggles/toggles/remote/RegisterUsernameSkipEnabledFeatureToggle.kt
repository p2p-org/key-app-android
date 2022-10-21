package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class RegisterUsernameSkipEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "android_onboarding_username_button_skip_enabled"
    override val featureDescription: String = "Enable skip on reserve username screen"
    override val defaultValue: Boolean = false
}
