package org.p2p.wallet.history.interactor.stream

abstract class AbstractStreamSource : HistoryStreamSource {

    override suspend fun nextItems(
        configuration: StreamSourceConfiguration
    ): List<HistoryStreamItem> {
        val sequence = mutableListOf<HistoryStreamItem>()
        while (true) {
            val item = next(configuration) ?: break
            sequence.add(item)
        }
        return sequence
    }
}
