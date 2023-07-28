package org.p2p.wallet.home.ui.container.mapper

import assertk.assertions.isEqualTo
import org.junit.Test
import java.math.BigDecimal
import org.p2p.wallet.utils.assertThat

class BalanceMapperTest {
    @Test
    fun `GIVEN different data TO check correct cases correct for $0`() {
        val zero = "$0"
        val mapper = BalanceMapper()
        val smallerThenOne1 = BigDecimal(0.001)
        val smallerThenOne2 = BigDecimal(0.01)
        val smallerThenOne3 = BigDecimal(0.05)
        val smallerThenOne4 = BigDecimal(0.1)
        val smallerThenOne5 = BigDecimal(0.5)
        val smallerThenOne6 = BigDecimal(0.8)
        val smallerThenOne7 = BigDecimal(0.9)
        mapper.mapBalanceForWallet(smallerThenOne1).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne2).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne3).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne4).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne5).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne6).assertThat().isEqualTo(zero)
        mapper.mapBalanceForWallet(smallerThenOne7).assertThat().isEqualTo(zero)
    }

    @Test
    fun `GIVEN different data TO check correct cases correct for $1 - $100`() {
        val mapper = BalanceMapper()
        val oneDollar = BigDecimal(1.264)
        val tenFifty = BigDecimal(10.50)
        val ninety = BigDecimal(90.20)
        val hundred = BigDecimal(100.1341)
        mapper.mapBalanceForWallet(oneDollar).assertThat().isEqualTo("$1")
        mapper.mapBalanceForWallet(tenFifty).assertThat().isEqualTo("$10")
        mapper.mapBalanceForWallet(ninety).assertThat().isEqualTo("$90")
        mapper.mapBalanceForWallet(hundred).assertThat().isEqualTo("$100")
    }

    @Test
    fun `GIVEN different data TO check correct cases correct for max`() {
        val mapper = BalanceMapper()
        val maxValue = BigDecimal(1000000000.1341)
        mapper.mapBalanceForWallet(maxValue).assertThat().isEqualTo("$999M+")
    }
}
