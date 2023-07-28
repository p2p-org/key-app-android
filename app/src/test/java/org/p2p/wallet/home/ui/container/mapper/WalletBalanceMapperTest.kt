package org.p2p.wallet.home.ui.container.mapper

import assertk.assertions.isEqualTo
import org.junit.Test
import java.math.BigDecimal
import org.p2p.wallet.utils.assertThat

class WalletBalanceMapperTest {
    @Test
    fun `GIVEN smaller than 1 balances WHEN map balance for wallet THAN balance is 0`() {
        val zero = "$0"
        val mapper = WalletBalanceMapper()
        val testValues = listOf(
            BigDecimal(0.001),
            BigDecimal(0.01),
            BigDecimal(0.05),
            BigDecimal(0.1),
            BigDecimal(0.5),
            BigDecimal(0.8),
            BigDecimal(0.9),
        )
        testValues.forEach {
            mapper.formatBalance(it).assertThat().isEqualTo(zero)
        }
    }

    @Test
    fun `GIVEN different data TO check correct cases correct for $1 - $100`() {
        val mapper = WalletBalanceMapper()
        val oneDollar = BigDecimal(1.264)
        val tenFifty = BigDecimal(10.50)
        val ninety = BigDecimal(90.20)
        val hundred = BigDecimal(100.1341)
        mapper.formatBalance(oneDollar).assertThat().isEqualTo("$1")
        mapper.formatBalance(tenFifty).assertThat().isEqualTo("$10")
        mapper.formatBalance(ninety).assertThat().isEqualTo("$90")
        mapper.formatBalance(hundred).assertThat().isEqualTo("$100")
    }

    @Test
    fun `GIVEN balance WHEN map balance THEN get MAX balance string`() {
        val mapper = WalletBalanceMapper()
        val maxValue = BigDecimal(1000000000.1341)
        mapper.formatBalance(maxValue).assertThat().isEqualTo("$999M+")
    }
}
