package org.p2p.wallet.utils

import io.mockk.every
import io.mockk.mockk
import org.p2p.wallet.common.feature_toggles.toggles.inapp.InAppFeatureFlag

inline fun <reified T : InAppFeatureFlag> mockInAppFeatureFlag(returnValue: Boolean = false): T {
    return mockk(relaxed = true) {
        every { featureValue } returns returnValue
    }
}
