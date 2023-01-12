package org.p2p.wallet.common.feature_toggles.remote_config

import androidx.core.content.edit
import android.content.SharedPreferences

class LocalFeatureToggleStorage(
    private val sharedPreferences: SharedPreferences
) {
    fun putFeatureToggle(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }

    operator fun contains(key: String): Boolean = sharedPreferences.contains(key)

    operator fun get(key: String): String? = sharedPreferences.getString(key, null)
}
