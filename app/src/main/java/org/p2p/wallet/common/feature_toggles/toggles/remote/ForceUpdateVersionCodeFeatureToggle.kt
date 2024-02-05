package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

class ForceUpdateVersionCodeFeatureToggle(
    valuesProvider: RemoteConfigValuesProvider
) : LongFeatureToggle(valuesProvider) {
    companion object {
        const val NOT_FETCHED = -1L
    }

    override val featureKey: String = "android_force_update_version_code"
    override val featureDescription: String = "Version code to force update to"
    override val defaultValue: Long = NOT_FETCHED
}
