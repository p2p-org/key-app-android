package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class NewSendEnabledFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "keyapp_new_send_enabled"
    override val featureDescription: String = "Enable New Send flow"
    override val defaultValue: Boolean = true // TODO PWN-6286 Disable on release if feature wont be passed in release
}
