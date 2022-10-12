package org.p2p.wallet.solend.storage

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

private const val KEY_IS_ABOUT_EARN_VIEWED = "KEY_IS_ABOUT_EARN_VIEWED"

class SolendStorage(
    sharedPreferences: SharedPreferences
) : SolendStorageContract {
    override var isAboutEarnOnboardingViewed: Boolean by BooleanPreference(
        preferences = sharedPreferences,
        key = KEY_IS_ABOUT_EARN_VIEWED,
        defaultValue = false
    )
}
