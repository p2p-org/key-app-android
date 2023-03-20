package org.p2p.wallet.deeplinks

import android.net.Uri
import org.p2p.wallet.deeplinks.DeeplinkHosts.ONBOARDING_DEEPLINK_SEGMENT

object DeeplinkUtils {
    fun isValidOnboardingLink(uri: Uri): Boolean {
        val segment = uri.pathSegments?.firstOrNull()
        return uri.host == ONBOARDING_DEEPLINK_SEGMENT && segment == DeeplinkSegments.onboardingSeedPhrase
    }
}

object DeeplinkHosts {
    const val ONBOARDING_DEEPLINK_SEGMENT = "onboarding"
}

object DeeplinkSegments {
    const val onboardingSeedPhrase = "seedPhrase"
}

object DeeplinkQuery {
    const val pinCode = "pincode"
    const val value = "value"
}
