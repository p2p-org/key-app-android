package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_STRIGA_SIMULATE_IBAN_NOT_FILLED = "KEY_STRIGA_SIMULATE_IBAN_NOT_FILLED"

class StrigaSimulateIbanNotFilledFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_STRIGA_SIMULATE_IBAN_NOT_FILLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs,
        key = KEY_STRIGA_SIMULATE_IBAN_NOT_FILLED,
        defaultValue = false
    )
}
