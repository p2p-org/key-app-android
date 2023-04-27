package org.p2p.wallet.infrastructure.coroutines

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
    condition: () -> Boolean
): Boolean {
    val endTime = System.currentTimeMillis() + timeoutMillis
    while (!condition() && System.currentTimeMillis() < endTime) {
        delay(100)
    }
    return condition()
}
