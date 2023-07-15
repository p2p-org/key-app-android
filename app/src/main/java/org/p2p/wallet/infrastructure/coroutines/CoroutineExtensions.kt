package org.p2p.wallet.infrastructure.coroutines

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.delay

/**
 * Waits for a condition to become true, with a timeout.
 *
 * @param condition A lambda that returns a Boolean representing the condition to wait for.
 * @param timeoutMillis The maximum amount of time to wait, in milliseconds.
 * @return True if the condition became true within the specified timeout, false otherwise.
 */
suspend fun waitForCondition(
    timeoutMillis: Long,
    condition: suspend () -> Boolean
): Boolean {
    val endTime = System.currentTimeMillis() + timeoutMillis
    while (!condition() && System.currentTimeMillis() < endTime) {
        delay(100)
    }
    return condition()
}

/**
 * Returns true if the current [CoroutineContext] has the [kotlinx.coroutines.test.TestCoroutineScheduler].
 */
val CoroutineContext.hasTestScheduler: Boolean
    get() = toString().contains("TestCoroutineScheduler")
