package org.p2p.wallet

import org.junit.Assert.assertEquals
import org.junit.Test

class DummyCodeForCoverageTest {

    @Test
    fun testDummyA() {
        val dummy = DummyCodeForCoverage()
        dummy.dummyA()
    }

    @Test
    fun testDummyB() {
        val dummy = DummyCodeForCoverage()
        assertEquals(42, dummy.dymmyB())
    }

    @Test
    fun testDummyC() {
        val dummy = DummyCodeForCoverage()
        assertEquals("42", dummy.dummyC(dummy.dymmyB()))
    }
}
