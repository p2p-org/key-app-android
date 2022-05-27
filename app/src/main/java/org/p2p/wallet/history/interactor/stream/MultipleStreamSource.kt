package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()
    private val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override suspend fun currentItem(): HistoryStreamItem? {
        val items = sources.mapNotNull { it.currentItem() }
        if (items.isEmpty()) {
            return null
        }
        var maxValue = items.first()
        for (item in items) {
            if (maxValue.streamSource!!.blockTime <= item.streamSource!!.blockTime) {
                maxValue = item
            }
        }
        return maxValue
    }

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        if (buffer.isEmpty()) {
            fillBuffer(configuration)
        }
        if (buffer.isEmpty()) {
            return null
        }
        return buffer.removeAt(0)
    }

    override fun reset() {
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) = withContext(executor) {
        supervisorScope {
            val items = sources.map {
                async { it.nextItems(configuration) }
            }.awaitAll().flatten()
            val sortedItems = items.sortedWith(compareBy { it.streamSource?.blockTime }).asReversed()
            buffer.addAll(sortedItems)
        }
    }
}
