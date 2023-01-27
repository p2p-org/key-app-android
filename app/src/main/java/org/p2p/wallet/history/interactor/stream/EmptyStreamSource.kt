package org.p2p.wallet.history.interactor.stream

class EmptyStreamSource : HistoryStreamSource {

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        return null
    }

    override suspend fun nextItems(
        configuration: StreamSourceConfiguration
    ): List<HistoryStreamItem> {
        return emptyList()
    }

    override suspend fun currentItem(): HistoryStreamItem? {
        return null
    }

    override fun reset() = Unit

    override fun isPagingReachedEnd(): Boolean = false
}
