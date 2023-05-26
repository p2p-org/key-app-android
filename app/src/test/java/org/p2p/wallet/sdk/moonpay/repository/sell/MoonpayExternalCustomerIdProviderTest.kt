package org.p2p.wallet.sdk.moonpay.repository.sell

import assertk.assertThat
import assertk.assertions.hasSize
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.network.provider.UserSeedPhraseDetails
import org.p2p.wallet.moonpay.repository.sell.MoonpayExternalCustomerIdProvider
import org.p2p.wallet.utils.mnemoticgenerator.English

class MoonpayExternalCustomerIdProviderTest {
    private val seedPhrase12Provider = mockk<SeedPhraseProvider> {
        every { getUserSeedPhrase() }.returns(
            UserSeedPhraseDetails(English.INSTANCE.words.take(12), SeedPhraseSource.MANUAL)
        )
    }

    private val seedPhrase24Provider = mockk<SeedPhraseProvider>() {
        every { getUserSeedPhrase() }.returns(
            UserSeedPhraseDetails(English.INSTANCE.words.take(24), SeedPhraseSource.MANUAL)
        )
    }

    @Test
    fun `GIVEN 12 words seed phrase WHEN every call THEN return same customer id`() {
        // given
        val customerIdProvider = MoonpayExternalCustomerIdProvider(seedPhrase12Provider)
        // when
        val generatedCustomerIds = (0..5)
            .map { customerIdProvider.getCustomerId().base58Value }
            .toSet()
        // then
        assertThat(generatedCustomerIds).hasSize(1)
    }

    @Test
    fun `GIVEN 24 words seed phrase WHEN every call THEN return same customer id`() {
        // given
        val customerIdProvider = MoonpayExternalCustomerIdProvider(seedPhrase24Provider)
        // when
        val generatedCustomerIds = (0..5)
            .map { customerIdProvider.getCustomerId().base58Value }
            .toSet()
        // then
        assertThat(generatedCustomerIds).hasSize(1)
    }
}
