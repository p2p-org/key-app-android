package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_DEBUG_FEATURE_TOGGLES_ENABLED = "KEY_DEBUG_FEATURE_TOGGLES_ENABLED"

class DebugTogglesFeatureFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_DEBUG_FEATURE_TOGGLES_ENABLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs,
        key = KEY_DEBUG_FEATURE_TOGGLES_ENABLED,
        defaultValue = false
    )
}
