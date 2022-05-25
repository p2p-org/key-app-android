package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.concurrent.Executors

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()
    private val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private var isPagingEnded = false
    override suspend fun currentItem(): HistoryStreamItem? {
        val item = sources.maxOf {
            val streamSource = it.currentItem()?.streamSource
            streamSource?.blockTime ?: (ZonedDateTime.now().toInstant().toEpochMilli() / 1000)
        }
        val latestItem = sources.firstOrNull { it.currentItem()?.streamSource?.blockTime == item }
        return latestItem?.currentItem()
    }

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        Timber.tag("HistoryInteractor").d("1 Start to fetch next batch = ${configuration.timeStampEnd}")
        if (buffer.isEmpty()) {
            Timber.tag("HistoryInteractor").d("2 Buffer is empty start to load new")
            fillBuffer(configuration)
        }
        Timber.tag("HistoryInteractor").d("3 buffer is updated, new size = ${buffer.size}")

        val item = buffer.firstOrNull()
        if (item != null) {
            buffer.removeAt(0)
        }
        return item
    }

    override fun reset() {
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) = withContext(executor + NonCancellable) {
        if (isPagingEnded) {
            return@withContext
        }
        Timber.tag("HistoryInteractor").d("4 Fill buffer")
        val items = sources.map {
            async { it.nextItems(configuration) }.also {
                it.invokeOnCompletion { throwable ->
                    if (throwable is EmptyDataException) {
                        isPagingEnded = true
                    }
                }
            }
        }.awaitAll().flatten()
        items.sortedWith(compareBy { it.streamSource?.blockTime })
        buffer.addAll(items)
        Timber.tag("HistoryInteractor").d("5 Buffer filled")
    }
}
