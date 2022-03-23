package org.p2p.wallet.utils

import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.IOException

private const val INITIAL_DELAY = 100L
private const val MAX_DELAY = 1000L
private const val FACTOR = 2.0
private const val RETRY_TIMES = 3

suspend fun <T> retryRequest(
    times: Int = RETRY_TIMES,
    initialDelay: Long = INITIAL_DELAY,
    maxDelay: Long = MAX_DELAY,
    factor: Double = FACTOR,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            Timber.tag("Coroutines").d("Retrying request since it failed")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}
