package org.p2p.wallet.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class DateTimeUtilsTest {

    @Test
    fun `GIVEN initial timer with 10 seconds expiration WHEN checkTimerIsOver THEN check time is not over`() {
        val savedTime = System.currentTimeMillis()
        val expiration = 10.seconds.inWholeMilliseconds

        assertFalse(
            DateTimeUtils.checkTimerIsOver(savedTime, expiration)
        )
    }

    @Test
    fun `GIVEN almost expired timer with 10 seconds expiration WHEN checkTimerIsOver THEN check time is not over`() {
        val expiration = 10.seconds.inWholeMilliseconds
        val savedTime = System.currentTimeMillis() - 9.5.seconds.inWholeMilliseconds

        assertFalse(
            DateTimeUtils.checkTimerIsOver(savedTime, expiration)
        )
    }

    @Test
    fun `GIVEN expired timer with 10 seconds expiration WHEN checkTimerIsOver THEN check time is over`() {
        val expiration = 10.seconds.inWholeMilliseconds
        val savedTime = System.currentTimeMillis() - expiration

        assertTrue(
            DateTimeUtils.checkTimerIsOver(savedTime, expiration)
        )
    }

    @Test
    fun `GIVEN very expired timer with 10 seconds expiration WHEN checkTimerIsOver THEN check time is over`() {
        val expiration = 10.seconds.inWholeMilliseconds
        val savedTime = System.currentTimeMillis() - 1.days.inWholeMilliseconds

        assertTrue(
            DateTimeUtils.checkTimerIsOver(savedTime, expiration)
        )
    }

    @Test
    fun `GIVEN unset time WHEN checkTimerIsOver THEN check time is over`() {
        val expiration = 10.seconds.inWholeMilliseconds
        val savedTime = 0L

        assertTrue(
            DateTimeUtils.checkTimerIsOver(savedTime, expiration)
        )
    }
}
