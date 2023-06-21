package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_STRIGA_SIMULATE_MOBILE_ALREADY_VERIFIED = "KEY_STRIGA_SIMULATE_MOBILE_ALREADY_VERIFIED"

class StrigaSimulateMobileAlreadyVerifiedFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_STRIGA_SIMULATE_MOBILE_ALREADY_VERIFIED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs,
        key = KEY_STRIGA_SIMULATE_MOBILE_ALREADY_VERIFIED,
        defaultValue = false
    )
}
