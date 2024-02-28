package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.BooleanPreference

class ReferralRewardTxMockFlag(prefs: SharedPreferences) : InAppFeatureFlag() {
    private companion object {
        private const val KEY_FLAG = "KEY_REFERRAL_REWARD_TX_MOCK"
    }
    override val featureName: String = KEY_FLAG
    override var featureValue: Boolean by BooleanPreference(
        preferences = prefs, key = KEY_FLAG, defaultValue = false
    )
}
