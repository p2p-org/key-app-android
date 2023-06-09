package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_STRIGA_SIMULATE_WEB3_ENABLED = "KEY_STRIGA_SIMULATE_WEB3_ENABLED"

class StrigaSimulateWeb3Flag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_STRIGA_SIMULATE_WEB3_ENABLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs,
        key = KEY_STRIGA_SIMULATE_WEB3_ENABLED,
        defaultValue = false
    )
}
