package org.p2p.wallet.history.interactor.stream

interface HistoryStreamSource {

    suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem?

    suspend fun nextItems(configuration: StreamSourceConfiguration): List<HistoryStreamItem>

    suspend fun currentItem(): HistoryStreamItem?

    fun reset()

    fun isPagingReachedEnd(): Boolean
}
