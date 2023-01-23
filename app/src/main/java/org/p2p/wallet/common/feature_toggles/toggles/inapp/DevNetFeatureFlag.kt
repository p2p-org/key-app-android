package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.BooleanPreference

private const val KEY_DEV_NET_ENABLED = "KEY_DEV_NET_ENABLED"

class DevNetFeatureFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    override val featureName: String = KEY_DEV_NET_ENABLED
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs, key = KEY_DEV_NET_ENABLED, defaultValue = BuildConfig.DEBUG
    )
}
