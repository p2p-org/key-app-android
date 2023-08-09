package org.p2p.wallet.home.ui.container.mapper

import assertk.assertions.isEqualTo
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import org.p2p.core.utils.DecimalFormatter
import org.p2p.wallet.utils.JvmDecimalFormatter
import org.p2p.wallet.utils.assertThat

class WalletBalanceMapperTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(DecimalFormatter::class)

        every { DecimalFormatter.format(any(), any()) } answers {
            JvmDecimalFormatter.format(arg(0), arg(1))
        }
    }

    @Test
    fun `GIVEN smaller than $0_01 balances WHEN map balance for wallet THAN balance is $0`() {
        val zero = "$0"
        val mapper = WalletBalanceMapper()
        val testValues = listOf(
            BigDecimal(0.001),
            BigDecimal(0.0014),
            BigDecimal(0.0026),
            BigDecimal(0.0098),
            BigDecimal(0.0054),
            BigDecimal(0.00991),
        )
        testValues.forEach {
            mapper.formatBalance(it).assertThat().isEqualTo(zero)
        }
    }

    @Test
    fun `GIVEN different data TO check correct cases correct for $0_01 - $1`() {
        val mapper = WalletBalanceMapper()
        val testValue1 = BigDecimal(0.05)
        val testValue2 = BigDecimal(0.1)
        val testValue3 = BigDecimal(0.5)
        val testValue4 = BigDecimal(0.8)
        val testValue5 = BigDecimal(0.9)
        mapper.formatBalance(testValue1).assertThat().isEqualTo("$0.05")
        mapper.formatBalance(testValue2).assertThat().isEqualTo("$0.1")
        mapper.formatBalance(testValue3).assertThat().isEqualTo("$0.5")
        mapper.formatBalance(testValue4).assertThat().isEqualTo("$0.8")
        mapper.formatBalance(testValue5).assertThat().isEqualTo("$0.9")
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
    fun `GIVEN different data TO check correct cases correct for K and M`() {
        val mapper = WalletBalanceMapper()
        val testK1 = BigDecimal(1353)
        val testK2 = BigDecimal(10244)
        val testM1 = BigDecimal(1005000)
        val testM2 = BigDecimal(8030000)
        mapper.formatBalance(testK1).assertThat().isEqualTo("$1K")
        mapper.formatBalance(testK2).assertThat().isEqualTo("$10K")
        mapper.formatBalance(testM1).assertThat().isEqualTo("$1M")
        mapper.formatBalance(testM2).assertThat().isEqualTo("$8M")
    }

    @Test
    fun `GIVEN balance WHEN map balance THEN get MAX balance string`() {
        val mapper = WalletBalanceMapper()
        val maxValue = BigDecimal(1000000000.1341)
        mapper.formatBalance(maxValue).assertThat().isEqualTo("$999M+")
    }
}
