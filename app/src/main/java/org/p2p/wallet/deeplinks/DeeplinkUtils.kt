package org.p2p.wallet.deeplinks

import android.net.Uri
import org.p2p.wallet.deeplinks.DeeplinkHosts.ONBOARDING_DEEPLINK_HOST

/**
 * Helpers for handling deeplinks.
 */
object DeeplinkUtils {
    /**
     * Checks whether passed Uri is a valid onboarding deeplink which opens an onboarding's seed phrase screen.
     */
    fun isValidOnboardingLink(uri: Uri): Boolean {
        val segment = uri.pathSegments?.firstOrNull()
        return uri.host == ONBOARDING_DEEPLINK_HOST && segment == DeeplinkSegments.onboardingSeedPhrase
    }

    /**
     * Checks whether passed Uri is a valid navigation deeplink which opens a specific tab in app.
     * Valid ones: keyapp://home, keyapp://swap et cetera.
     * @see DeeplinkTarget.fromScreenName
     */
    fun isValidNavigationLink(uri: Uri): Boolean {
        return DeeplinkTarget.fromScreenName(uri.host) != null
    }
}

/**
 * Storage for deeplink hosts (i.e. scheme://{host}/segments).
 */
object DeeplinkHosts {
    const val ONBOARDING_DEEPLINK_HOST = "onboarding"
}

/**
 * Storage for deeplink path segments (i.e. scheme://host/{segments}).
 */
object DeeplinkSegments {
    const val onboardingSeedPhrase = "seedPhrase"
}

/**
 * Storage for deeplink query parameters (i.e. scheme://host/segments?{query}).
 */
object DeeplinkQuery {
    const val pinCode = "pincode"
    const val value = "value"
}
