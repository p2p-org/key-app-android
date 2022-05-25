package org.p2p.wallet.history.interactor.stream

import org.p2p.wallet.history.model.RpcTransactionSignature

class EmptyStreamSource : HistoryStreamSource {

    override suspend fun next(configuration: StreamSourceConfiguration): Pair<String, RpcTransactionSignature?> {
        return Pair("", null)
    }

    override suspend fun nextItems(
        configuration: StreamSourceConfiguration
    ): List<Pair<String, RpcTransactionSignature>> {
        return emptyList()
    }

    override suspend fun currentItem(): Pair<String, RpcTransactionSignature?> {
        return Pair("", null)
    }

    override fun reset() {
        // TODO make something
    }
}
