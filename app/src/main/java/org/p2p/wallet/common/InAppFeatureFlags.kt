package org.p2p.wallet.common

import android.content.SharedPreferences
import org.p2p.wallet.common.feature_toggles.toggles.inapp.DebugTogglesFeatureFlag
import org.p2p.wallet.common.feature_toggles.toggles.inapp.DevNetFeatureFlag
import org.p2p.wallet.common.feature_toggles.toggles.inapp.InAppFeatureFlag
import org.p2p.wallet.common.feature_toggles.toggles.inapp.PollingFeatureFlag

class InAppFeatureFlags(prefs: SharedPreferences) {
    val isPollingEnabled = PollingFeatureFlag(prefs)
    var isDevNetEnabled = DevNetFeatureFlag(prefs)

    /**
     * Allows to override values from FirebaseRemoteConfig
     */
    var isDebugRemoteConfigEnabled = DebugTogglesFeatureFlag(prefs)

    val allInAppFeatureFlags: List<InAppFeatureFlag> = listOf(
        isPollingEnabled,
        isDevNetEnabled,
        isDebugRemoteConfigEnabled
    )

    fun findFeatureFlagByName(featureName: String): InAppFeatureFlag? {
        return allInAppFeatureFlags.find { it.featureName == featureName }
    }
}
