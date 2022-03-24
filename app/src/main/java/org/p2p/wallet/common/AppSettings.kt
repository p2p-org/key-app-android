package org.p2p.wallet.common

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.BuildConfig

private const val KEY_POLLING_ENABLED = "KEY_POLLING_ENABLED"
private const val KEY_IS_PROD = "KEY_IS_PROD"

class AppSettings(private val sharedPreferences: SharedPreferences) {

    val isPoolingEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_POLLING_ENABLED, true)

    fun setPollingEnabled(isEnabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_POLLING_ENABLED, isEnabled) }
    }

    val isProd: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_PROD, BuildConfig.IS_PROD)

    fun setIsProd(isProd: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_IS_PROD, isProd) }
    }
}
