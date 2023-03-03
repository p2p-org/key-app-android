package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class SendViaLinkFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "send_via_link_enabled"
    override val featureDescription: String = "Enabled Sending via link"
    override val defaultValue: Boolean = false
}
