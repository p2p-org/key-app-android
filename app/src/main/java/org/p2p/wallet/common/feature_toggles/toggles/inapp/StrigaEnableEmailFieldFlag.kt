package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_STRIGA_ENABLE_EMAIL_FIELD = "KEY_STRIGA_ENABLE_EMAIL_FIELD"

class StrigaEnableEmailFieldFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_STRIGA_ENABLE_EMAIL_FIELD
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs,
        key = KEY_STRIGA_ENABLE_EMAIL_FIELD,
        defaultValue = false
    )
}
