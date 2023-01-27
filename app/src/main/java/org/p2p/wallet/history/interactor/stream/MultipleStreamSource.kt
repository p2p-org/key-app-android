package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.wallet.common.di.ServiceScope
import timber.log.Timber

private const val TAG = "HistoryStreamSource"

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>,
    private val serviceScope: ServiceScope
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()

    override suspend fun currentItem(): HistoryStreamItem? = withContext((serviceScope.coroutineContext)) {
        var maxValue: HistoryStreamItem?
        return@withContext try {
            val items = sources.map { async { it.currentItem() } }.awaitAll()
            maxValue = items.firstOrNull()
            for (item in items) {
                if (item?.streamSource?.blockTime == null) {
                    continue
                }
                if (maxValue?.streamSource!!.blockTime <= item.streamSource.blockTime) {
                    maxValue = item
                }
            }
            maxValue
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to get nextItem")
            null
        }
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

    override fun isPagingReachedEnd(): Boolean {
        return sources.all { it.isPagingReachedEnd() }
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) =
        withContext(serviceScope.coroutineContext) {
            val items = sources.map {
                async { it.nextItems(configuration) }
            }
                .awaitAll()
                .flatten()
            val sortedItems = items.sortedWith(compareBy { it.streamSource?.blockTime })
                .asReversed()
            buffer.addAll(sortedItems)
        }
}
