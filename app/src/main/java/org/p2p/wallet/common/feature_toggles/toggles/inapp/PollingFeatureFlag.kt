package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_POLLING_ENABLED = "KEY_POLLING_ENABLED"

class PollingFeatureFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_POLLING_ENABLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs, key = KEY_POLLING_ENABLED, defaultValue = true
    )
}
