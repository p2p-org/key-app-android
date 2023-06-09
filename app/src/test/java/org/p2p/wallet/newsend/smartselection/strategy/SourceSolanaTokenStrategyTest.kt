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
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
internal class SourceSolanaTokenStrategyTest {

    private val minRentExemption = BigInteger.valueOf(890880)

    @Test
    fun `GIVEN non-solana token WHEN strategy started THEN return false`() = runTest {
        val splToken: Token.Active = mockk()
        every { splToken.isSOL } returns false

        val strategy = SourceSolanaTokenStrategy(
            sourceToken = splToken,
            recipient = mockk(),
            inputAmount = mockk(),
            fee = mockk(),
            minRentExemption = minRentExemption
        )

        assertThat(strategy.isPayable()).isFalse()
    }

    @Test
    fun `GIVEN token WHEN balance is enough THEN return true`() = runTest {
        val sourceToken: Token.Active = mockk()
        val fee: FeeRelayerFee = mockk()
        val inputAmount = BigDecimal.TEN
        val totalFee = BigInteger.valueOf(100000)

        every { sourceToken.isSOL } returns true
        every { sourceToken.decimals } returns 9
        every { sourceToken.totalInLamports } returns totalFee + inputAmount.toLamports(9)
        every { fee.totalInSol } returns totalFee

        val strategy = SourceSolanaTokenStrategy(
            sourceToken = sourceToken,
            recipient = mockk(),
            inputAmount = inputAmount,
            fee = fee,
            minRentExemption = minRentExemption
        )

        assertThat(strategy.isPayable()).isTrue()
    }

    @Test
    fun `GIVEN valid data WHEN execution started THEN return FeePayerState`() = runTest {
        val sourceToken: Token.Active = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)

        every { sourceToken.isSOL } returns true
        every { sourceToken.decimals } returns 9
        every { sourceToken.tokenSymbol } returns "SOL"
        every { sourceToken.totalInLamports } returns BigInteger.valueOf(200000000)

        val strategy = SourceSolanaTokenStrategy(
            sourceToken = sourceToken,
            recipient = mockk(),
            inputAmount = inputAmount,
            fee = mockk(),
            minRentExemption = minRentExemption
        )

        val state = strategy.execute()
        assertThat(state).isInstanceOf(FeePayerState.CalculationSuccess::class)
    }
}
