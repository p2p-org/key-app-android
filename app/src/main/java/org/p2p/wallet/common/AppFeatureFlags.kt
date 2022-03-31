package org.p2p.wallet.common

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.BuildConfig

private const val KEY_POLLING_ENABLED = "KEY_POLLING_ENABLED"
private const val KEY_DEV_NET_ENABLED = "KEY_DEV_NET_ENABLED"

class AppFeatureFlags(private val sharedPreferences: SharedPreferences) {

    val isPollingEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_POLLING_ENABLED, true)

    fun setPollingEnabled(isEnabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_POLLING_ENABLED, isEnabled) }
    }

    val isDevnetEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DEV_NET_ENABLED, BuildConfig.KEY_DEV_NET_ENABLED)

    fun setIsDevnetEnabled(isDevnetEnabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_DEV_NET_ENABLED, isDevnetEnabled) }
    }
}
