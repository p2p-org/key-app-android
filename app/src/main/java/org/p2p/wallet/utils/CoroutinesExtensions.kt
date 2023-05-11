package org.p2p.wallet.utils

import timber.log.Timber
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.reflect.KClass
import kotlinx.coroutines.delay

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
            Timber.tag("Coroutines").i(e)
            Timber.tag("Coroutines").i("Retrying request since it failed")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}

/**
 * Retries [block] if it throws an exception of type [exceptionTypes] up to [maxAttempts] times.
 * When [maxAttempts] is reached or the exception is not of type [exceptionTypes], the exception is rethrown.
 *
 * @param exceptionTypes the types of exceptions to retry on
 * (default: [SocketTimeoutException], [InterruptedIOException])
 * @param maxAttempts the maximum number of attempts to retry (default: 3)
 * @param delayMillis the delay between attempts (default: 1 second)
 * @param block the block to execute
 * @return the result of [block]
 */
suspend inline fun <T> retryOnException(
    exceptionTypes: Set<KClass<out Throwable>> = setOf(
        SocketTimeoutException::class,
        InterruptedIOException::class,
        IOException::class
    ),
    maxAttempts: Int = 3,
    delayMillis: Long = 1000,
    crossinline block: suspend () -> T
): T {
    var attempts = 0
    val handleException: suspend (Throwable) -> Unit = { e: Throwable ->
        Timber.tag("Coroutines").i(
            "caught exception during execution attempt: " +
                "${e.javaClass}(message=${e.message}); retrying: $attempts"
        )
        if (++attempts >= maxAttempts || exceptionTypes.none { it.isInstance(e) }) {
            throw e
        }
        delay(delayMillis)
    }
    while (true) {
        try {
            return block()
        } catch (e: Throwable) {
            if (exceptionTypes.any { it.isInstance(e) }) {
                handleException(e)
            } else {
                throw e
            }
        }
    }
}
