package org.p2p.wallet.wallet.deeplinks

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.DeeplinkUtils

/**
 * Tests for DeeplinkUtils helpers.
 */
class DeeplinkUtilsTest {

    private fun newMockUri(scheme: String, host: String, path: String? = null, query: String? = null): Uri {
        return mockk {
            every { getScheme() } returns scheme
            every { getHost() } returns host
            every { getPath() } returns path
            every { pathSegments } returns (path?.split("/")?.filter { it.isNotBlank() }.orEmpty())
            every { getQuery() } returns query
        }
    }

    @Test
    fun `GIVEN onboarding deeplink WHEN deeplink is valid THEN isValidOnboardingLink return true`() {
        var uri: Uri = newMockUri("https", "onboarding", "/seedPhrase", "")
        assertTrue(DeeplinkUtils.isValidOnboardingLink(uri))

        uri = newMockUri("keyapp", "onboarding", "/seedPhrase", "")
        assertTrue(DeeplinkUtils.isValidOnboardingLink(uri))

        uri = newMockUri("malicious", "onboarding", "/seedPhrase", "")
        assertTrue(DeeplinkUtils.isValidOnboardingLink(uri))

        uri = newMockUri("keyapp", "onBoarding", "/seedPhrase", "")
        assertFalse(DeeplinkUtils.isValidOnboardingLink(uri))

        uri = newMockUri("keyapp", "onboarding", "/seedPhraseQ", "")
        assertFalse(DeeplinkUtils.isValidOnboardingLink(uri))

        uri = newMockUri("keyapp", "navigate", "/swap", "?hello=1&world=2")
        assertFalse(DeeplinkUtils.isValidOnboardingLink(uri))
    }

    @Test
    fun `GIVEN navigation deeplink WHEN deeplink is valid THEN isValidCommonLink return true`() {
        var uri: Uri = newMockUri("keyapp", DeeplinkTarget.HOME.screenName)
        assertTrue(DeeplinkUtils.isValidCommonLink(uri))

        uri = newMockUri("keyapp", "earn", "")
        assertFalse(DeeplinkUtils.isValidCommonLink(uri))

        uri = newMockUri("keyapp", DeeplinkTarget.HISTORY.screenName)
        assertTrue(DeeplinkUtils.isValidCommonLink(uri))

        uri = newMockUri("keyapp", "feedback")
        assertFalse(DeeplinkUtils.isValidCommonLink(uri))

        uri = newMockUri("keyapp", "homeQ", "")
        assertFalse(DeeplinkUtils.isValidCommonLink(uri))

        uri = newMockUri("keyapp", "", "home")
        assertFalse(DeeplinkUtils.isValidCommonLink(uri))
    }
}
