package org.p2p.wallet.common

import android.content.SharedPreferences
import org.p2p.wallet.BuildConfig

private const val KEY_POLLING_ENABLED = "KEY_POLLING_ENABLED"
private const val KEY_DEV_NET_ENABLED = "KEY_DEV_NET_ENABLED"
private const val KEY_COIN_GECKO_ENABLED = "KEY_DEV_NET_ENABLED"

class AppFeatureFlags(prefs: SharedPreferences) {
    var isPollingEnabled: Boolean by BooleanPreference(prefs, KEY_POLLING_ENABLED, true)
    var isDevnetEnabled: Boolean by BooleanPreference(prefs, KEY_DEV_NET_ENABLED, BuildConfig.KEY_DEV_NET_ENABLED)
    var useCoinGeckoForPrices: Boolean by BooleanPreference(prefs, KEY_COIN_GECKO_ENABLED, false)
}
