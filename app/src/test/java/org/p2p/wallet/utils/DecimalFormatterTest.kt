package org.p2p.wallet.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import org.p2p.core.utils.formatToken

class DecimalFormatterTest {

    @Test
    fun testFormatPreservingDecimals() {
        val value = BigDecimal("10.0")
        val result = value.formatToken(9, noStrip = true, keepInitialDecimals = true)
        assertEquals("10.0", result)
    }
}
