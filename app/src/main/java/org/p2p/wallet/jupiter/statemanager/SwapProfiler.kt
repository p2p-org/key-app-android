package org.p2p.wallet.jupiter.statemanager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val KEY_DIFF_ROUTE = "route"
private const val KEY_DIFF_TX = "tx"

class SwapProfiler {
    // since the map might be accessed from different coroutines, it's safer to synchronize it
    private val mutex = Mutex()
    private val map = mutableMapOf<String, Long>()

    /**
     * Set the time when routes were fetched
     */
    suspend fun setRoutesFetchedTime() = setTimePoint(KEY_DIFF_ROUTE)

    /**
     * Set the time when transaction for route was created
     */
    suspend fun setTxCreatedTime() = setTimePoint(KEY_DIFF_TX)

    /**
     * Get the time difference between current time and the time when routes were fetched
     * @return time difference in seconds
     */
    suspend fun getRouteFetchedTimeDiffSeconds(): Long = getTimeDiffSeconds(KEY_DIFF_ROUTE)

    /**
     * Get the time difference between current time and the time when transaction for route was created
     * @return time difference in seconds
     */
    suspend fun getTxCreatedTimeDiffSeconds(): Long = getTimeDiffSeconds(KEY_DIFF_TX)

    private suspend fun getTimeDiffSeconds(tag: String): Long {
        val lastPoint = getTimePoint(tag)
        if (lastPoint == 0L) return 0

        return ((System.currentTimeMillis() - getTimePoint(tag)) / 1000.0).toLong()
    }

    private suspend fun getTimePoint(tag: String): Long = mutex.withLock {
        map[tag] ?: 0
    }

    private suspend fun setTimePoint(tag: String) = mutex.withLock {
        map[tag] = System.currentTimeMillis()
    }
}
