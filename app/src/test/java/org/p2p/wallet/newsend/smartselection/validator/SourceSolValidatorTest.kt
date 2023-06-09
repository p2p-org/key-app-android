package org.p2p.wallet.newsend.smartselection.validator

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.token.Token
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
internal class SourceSolValidatorTest {

    private val minRentExemption = BigInteger.valueOf(890880)

    @Test
    fun `GIVEN non-solana token WHEN validation started THEN return false`() = runTest {
        val splToken: Token.Active = mockk()
        every { splToken.isSOL } returns false

        val validator = SourceSolanaNegativeValidator(
            sourceToken = splToken,
            recipient = mockk(),
            inputAmount = mockk(),
            minRentExemption = minRentExemption
        )

        assertThat(validator.isAmountInvalidForRecipient()).isFalse()
        assertThat(validator.isAmountInvalidForSender()).isFalse()
        assertThat(validator.isLowMinBalanceIgnored()).isFalse()
    }

    @Test
    fun `GIVEN too low amount WHEN recipient has empty balance THEN return true `() = runTest {
        val solToken: Token.Active = mockk()
        val recipient = SearchResult.AddressFound("someaddress", balance = 0L)
        every { solToken.isSOL } returns true

        // any amount lower than [minRentExemption]
        val inputAmount = minRentExemption - BigInteger.TEN

        val validator = SourceSolanaNegativeValidator(
            sourceToken = solToken,
            recipient = recipient,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )

        assertThat(validator.isAmountInvalidForRecipient()).isTrue()
    }

    @Test
    fun `GIVEN amount WHEN sender sends all sol THEN return true`() = runTest {
        val solToken: Token.Active = mockk()
        val recipient = SearchResult.AddressFound("someaddress", balance = 0L)

        every { solToken.isSOL } returns true
        every { solToken.totalInLamports } returns minRentExemption

        val inputAmount = BigInteger.valueOf(100L)
        val validator = SourceSolanaNegativeValidator(
            sourceToken = solToken,
            recipient = recipient,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )

        assertThat(validator.isAmountInvalidForSender()).isTrue()
    }

    @Test
    fun `GIVEN amount WHEN sender has ignore min required balance AND recipient empty THEN return true`() = runTest {
        val solToken: Token.Active = mockk()
        val recipient = SearchResult.AddressFound("someaddress", balance = 0L)

        every { solToken.isSOL } returns true
        every { solToken.totalInLamports } returns minRentExemption

        val inputAmount = BigInteger.valueOf(100L)
        val validator = SourceSolanaNegativeValidator(
            sourceToken = solToken,
            recipient = recipient,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )

        assertThat(validator.isLowMinBalanceIgnored()).isTrue()
    }

    @Test
    fun `GIVEN amount WHEN sender has ignore min required balance THEN return true`() = runTest {
        val solToken: Token.Active = mockk()
        val recipient = SearchResult.AddressFound("someaddress", balance = 10L)
        val inputAmount = BigInteger.valueOf(100L)

        every { solToken.isSOL } returns true
        every { solToken.totalInLamports } returns minRentExemption - inputAmount

        val validator = SourceSolanaNegativeValidator(
            sourceToken = solToken,
            recipient = recipient,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )

        assertThat(validator.isLowMinBalanceIgnored()).isTrue()
    }
}

