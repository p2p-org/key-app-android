package org.p2p.wallet.jupiter.statemanager

import org.junit.Test
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class SwapProfilerTest {

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Test
    fun `GIVEN swap profiler WHEN routes fetch time never set THEN check diff is 0`() = runTest {
        val job = launch(dispatcher) {
            val profiler = SwapProfiler()
            delay(1000)

            val timeDiff = profiler.getRouteFetchedTimeDiffSeconds()

            assertEquals(0, timeDiff)
        }
        job.join()
    }

    @Test
    fun `GIVEN swap profiler WHEN routes fetch in 1 second THEN check diff is 1`() = runTest {
        val job = launch(dispatcher) {
            val profiler = SwapProfiler()
            profiler.setRoutesFetchedTime()

            // Delay to simulate time difference
            delay(1000)

            val timeDiff = profiler.getRouteFetchedTimeDiffSeconds()

            assertEquals(1, timeDiff)
        }
        job.join()
    }

    @Test
    fun `GIVEN swap profiler WHEN tx created in 2 seconds THEN check diff is 2`() = runTest {
        val job = launch(dispatcher) {
            val profiler = SwapProfiler()
            profiler.setTxCreatedTime()

            // Delay to simulate time difference
            delay(2000)

            val timeDiff = profiler.getTxCreatedTimeDiffSeconds()

            assertEquals(2, timeDiff)
        }
        job.join()
    }
}
