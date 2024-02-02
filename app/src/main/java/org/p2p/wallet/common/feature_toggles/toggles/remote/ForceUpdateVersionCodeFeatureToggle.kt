package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class ForceUpdateVersionCodeFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : IntFeatureToggle(valuesProvider) {
    companion object {
        private const val NOT_FETCHED = -1
    }

    override val featureKey: String = "android_force_update_version_code"
    override val featureDescription: String = "Version code to force update to"
    override val defaultValue: Int = NOT_FETCHED
}
