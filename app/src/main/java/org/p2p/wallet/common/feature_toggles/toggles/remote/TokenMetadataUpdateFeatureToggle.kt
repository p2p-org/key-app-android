package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class TokenMetadataUpdateFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : BooleanFeatureToggle(valuesProvider) {
    override val featureKey: String = "android_force_token_metadata_update"
    override val featureDescription: String = "Force update token metadata json file"
    override val defaultValue: Boolean = false
}
