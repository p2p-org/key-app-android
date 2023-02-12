package org.p2p.wallet.deeplinks

import android.net.Uri

object DeeplinkHosts {
    private const val ONBOARDING_DEEPLINK_SEGMENT = "onboarding"

    val validListToStartDeeplinks = listOf(ONBOARDING_DEEPLINK_SEGMENT)
}

object DeeplinkSegments {
    const val onboardingSeedPhrase = "seedPhrase"
}

object DeeplinkQuery {
    const val pinCode = "pincode"
    const val value = "value"
}

object DeeplinkUtils {
    fun hasFastOnboardingDeeplink(uri: Uri?): Boolean {
        return uri?.pathSegments?.first() == DeeplinkSegments.onboardingSeedPhrase
    }
}
