package org.p2p.wallet.common.feature_toggles.toggles.inapp

import android.content.SharedPreferences
import org.p2p.wallet.common.StringPreference

private const val STRIGA_KYC_BANNER_MOCK_FLAG = "STRIGA_KYC_BANNER_MOCK_FLAG"

class StrigaKycBannerMockFlag(prefs: SharedPreferences) : InAppFeatureFlag.InAppFeatureFlagString() {
    override val featureName: String = STRIGA_KYC_BANNER_MOCK_FLAG
    override var featureValueString: String? by StringPreference(
        preferences = prefs,
        key = STRIGA_KYC_BANNER_MOCK_FLAG,
        defaultValue = null
    )
}
