package org.p2p.wallet.newsend.smartselection.strategy

import assertk.assertThat
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
internal class SolanaTokenStrategyTest {

    @Test
    fun `GIVEN fee WHEN sol is fee payer THEN check sol balance`() = runTest {
        val splToken: Token.Active = mockk()
        val solToken: Token.Active = mockk()
        val fee: FeeRelayerFee = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)

        every { splToken.isSOL } returns false
        every { solToken.isSOL } returns true
        every { splToken.totalInLamports } returns BigInteger.valueOf(20000000)
        every { solToken.totalInLamports } returns BigInteger.valueOf(20000000)
        every { fee.totalInSol } returns BigInteger.valueOf(1000000)

        val strategy = SolanaTokenStrategy(
            solToken = solToken,
            sourceToken = splToken,
            inputAmount = inputAmount,
            fee = fee
        )

        assertThat(strategy.isPayable()).isTrue()
    }

    @Test
    fun `GIVEN valid data WHEN execution started THEN return FeePayerState`() = runTest {
        val splToken: Token.Active = mockk()
        val solToken: Token.Active = mockk()
        val inputAmount = BigDecimal.valueOf(0.1)
        val fee: FeeRelayerFee = mockk()

        every { splToken.isSOL } returns false
        every { splToken.tokenSymbol } returns "USDC"
        every { solToken.isSOL } returns true

        val strategy = SolanaTokenStrategy(
            solToken = splToken,
            sourceToken = splToken,
            inputAmount = inputAmount,
            fee = fee
        )

        assertThat(strategy.execute()).isInstanceOf(FeePayerState.CalculationSuccess::class)
    }
}
