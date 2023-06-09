package org.p2p.wallet.newsend.smartselection.strategy

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
internal class SourceSplTokenStrategyTest {

    @Test
    fun `GIVEN sol WHEN checking balance THEN return false`() = runTest {
        val sourceToken: Token.Active = mockk()

        every { sourceToken.isSOL } returns true

        val strategy = SourceSplTokenStrategy(
            sourceToken = sourceToken,
            inputAmount = mockk(),
            fee = mockk()
        )

        assertThat(strategy.isPayable()).isFalse()
    }

    @Test
    fun `GIVEN input data WHEN spl balance is not enough THEN return false`() = runTest {
        val sourceToken: Token.Active = mockk()
        val fee: FeeRelayerFee = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)

        every { sourceToken.isSOL } returns false
        every { sourceToken.decimals } returns 6
        every { sourceToken.totalInLamports } returns BigInteger.valueOf(1000020)
        every { fee.totalInSpl } returns BigInteger.valueOf(1000000)

        val strategy = SourceSplTokenStrategy(
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee
        )

        assertThat(strategy.isPayable()).isFalse()
    }

    @Test
    fun `GIVEN input data WHEN spl balance is enough THEN return true`() = runTest {
        val sourceToken: Token.Active = mockk()
        val fee: FeeRelayerFee = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)

        every { sourceToken.isSOL } returns false
        every { sourceToken.decimals } returns 6
        every { sourceToken.totalInLamports } returns BigInteger.valueOf(20000000)
        every { fee.totalInSpl } returns BigInteger.valueOf(1000000)

        val strategy = SourceSplTokenStrategy(
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee
        )

        assertThat(strategy.isPayable()).isTrue()
    }

    @Test
    fun `GIVEN valid data WHEN execution started THEN return FeePayerState`() = runTest {
        val sourceToken: Token.Active = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)

        every { sourceToken.tokenSymbol } returns "USDC"

        val strategy = SourceSplTokenStrategy(
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = mockk()
        )

        val state = strategy.execute()
        assertThat(state).isInstanceOf(FeePayerState.CalculationSuccess::class)
    }
}
