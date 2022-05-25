package org.p2p.wallet.history.interactor.stream

import org.threeten.bp.ZonedDateTime

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()

    override suspend fun currentItem(): HistoryStreamItem? {
        val item = sources.maxOf {
            it.currentItem()?.streamSource?.blockTime ?: (ZonedDateTime.now().toInstant().toEpochMilli() / 1000)
        }
        val latestItem = sources.firstOrNull { it.currentItem()?.streamSource?.blockTime == item }
        return latestItem?.currentItem()
    }

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        if (buffer.isEmpty()) {
            fillBuffer(configuration)
        }
        val item = buffer.firstOrNull()
        if (item != null) {
            buffer.removeAt(0)
        }
        return item
    }

    override fun reset() {
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) {
        val items = sources.flatMap { it.nextItems(configuration) }
        items.sortedWith(compareBy { it.streamSource?.blockTime })
        buffer.addAll(items)
    }
}
