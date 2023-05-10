package org.p2p.wallet.utils

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoroutinesExtensionsTest {
    @Test
    fun `GIVEN result without error WHEN retryOnException THEN exception is not thrown`() = runBlocking {
        val result = retryOnException {
            "hello world"
        }

        assertEquals("hello world", result)
    }

    @Test
    fun `GIVEN 3 exceptions, 5 attempts WHEN retryOnException THEN 3 exceptions caught and 4th result returned`() = runBlocking {
        val exception1 = SocketTimeoutException("timeout exception")
        val exception2 = InterruptedIOException("interrupted exception")
        val exception3 = IllegalArgumentException("illegal argument exception")

        val mockBlock = mockk<suspend () -> String>()
        coEvery { mockBlock() } throws exception1 andThenThrows exception2 andThenThrows exception3 andThen "result"

        val result = retryOnException(
            exceptionTypes = setOf(
                SocketTimeoutException::class,
                InterruptedIOException::class,
                IllegalArgumentException::class
            ),
            maxAttempts = 5,
            delayMillis = 100
        ) {
            mockBlock()
        }

        assertEquals("result", result)
        coVerify(exactly = 4) { mockBlock() }
    }

    @Test
    fun `GIVEN unrecoverable block and 2 attempts WHEN retryOnException THEN check supported exception rethrown`() = runBlocking {
        val exception1 = SocketTimeoutException("timeout exception")
        val mockBlock = mockk<suspend () -> String>()
        coEvery { mockBlock() } throws exception1

        assertFailsWith<SocketTimeoutException> {
            retryOnException(
                exceptionTypes = setOf(SocketTimeoutException::class),
                maxAttempts = 2,
                delayMillis = 100
            ) {
                mockBlock()
            }
        }
        coVerify(exactly = 2) { mockBlock() }
    }

    @Test
    fun `GIVEN unsupported exception WHEN retryOnException THEN check throws without retry unsupported exception`() = runBlocking {
        // this exception isn't listed in supported exceptions, so it should be thrown at any time
        val exception3 = IllegalArgumentException("illegal argument exception")
        val mockBlock = mockk<suspend () -> String>()
        coEvery { mockBlock() } throws exception3

        assertFailsWith<IllegalArgumentException> {
            retryOnException(
                exceptionTypes = setOf(SocketTimeoutException::class, InterruptedIOException::class),
                maxAttempts = 2,
                delayMillis = 100
            ) {
                mockBlock()
            }
        }
        coVerify(exactly = 1) { mockBlock() }
    }
}
