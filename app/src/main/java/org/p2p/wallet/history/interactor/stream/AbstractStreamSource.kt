package org.p2p.wallet.history.interactor.stream

import timber.log.Timber

private const val TAG = "HistoryStreamSource"

abstract class AbstractStreamSource : HistoryStreamSource {

    override suspend fun nextItems(
        configuration: StreamSourceConfiguration
    ): List<HistoryStreamItem> {
        val sequence = mutableListOf<HistoryStreamItem>()
        return try {
            while (true) {
                val item = next(configuration) ?: break
                sequence.add(item)
            }
            sequence
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to fetch next items")
            emptyList()
        }
    }
}
