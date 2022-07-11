package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_COIN_GECKO_ENABLED = "KEY_COIN_GECKO_ENABLED"

class CoinGeckoFeatureFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_COIN_GECKO_ENABLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs, key = KEY_COIN_GECKO_ENABLED, defaultValue = false
    )
}
