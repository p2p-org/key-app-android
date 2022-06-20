package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.p2p.wallet.common.di.ServiceScope

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>,
    private val serviceScope: ServiceScope
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()

    override suspend fun currentItem(): HistoryStreamItem? = withContext(context = serviceScope.coroutineContext) {
        var maxValue: HistoryStreamItem?

        val items = sources.map {
            async { it.currentItem() }
        }
            .awaitAll()
            .filterNotNull()

        if (items.isEmpty()) {
            return@withContext null
        }

        maxValue = items.firstOrNull()
        for (item in items) {
            if (maxValue?.streamSource!!.blockTime <= item.streamSource!!.blockTime) {
                maxValue = item
            }
        }
        return@withContext maxValue
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
        buffer.clear()
        sources.forEach { it.reset() }
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) = supervisorScope {
        val items = sources.map {
            async { it.nextItems(configuration) }
        }.awaitAll().flatten()
        val sortedItems = items.sortedWith(compareBy { it.streamSource?.blockTime }).asReversed()
        buffer.addAll(sortedItems)
    }
}
