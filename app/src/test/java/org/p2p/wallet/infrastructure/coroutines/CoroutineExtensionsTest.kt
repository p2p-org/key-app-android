package org.p2p.wallet.infrastructure.coroutines

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CoroutineExtensionsTest {

    @Test
    fun `GIVEN waitForCondition WHEN condition is met THEN returns true`() = runBlocking {
        var conditionMet = false
        var endTime = System.currentTimeMillis()

        // Start a coroutine to set the condition to true after a delay
        launch {
            delay(500)
            endTime = System.currentTimeMillis() - endTime
            conditionMet = true
        }

        // Wait for the condition to be true
        val result = waitForCondition(1000) { conditionMet }

        // Assert that the condition was met
        assertTrue(result)
        assertTrue(endTime >= 500)
    }

    @Test
    fun `GIVEN waitForCondition WHEN condition is not met THEN returns false`() = runBlocking {
        val conditionMet = false

        // Wait for the condition to be true
        val result = waitForCondition(500) { conditionMet }

        // Assert that the result is false, since the condition was not met
        assertFalse(result)
    }
}
