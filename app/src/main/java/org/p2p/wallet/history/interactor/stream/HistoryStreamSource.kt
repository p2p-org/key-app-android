package org.p2p.wallet.history.interactor.stream

import org.p2p.wallet.history.model.RpcTransactionSignature

interface HistoryStreamSource {

    suspend fun next(configuration: StreamSourceConfiguration): Pair<String, RpcTransactionSignature?>

    suspend fun nextItems(configuration: StreamSourceConfiguration): List<Pair<String, RpcTransactionSignature>>

    suspend fun currentItem(): Pair<String, RpcTransactionSignature?>

    fun reset()
}

class StreamSourceConfiguration(val timeStampEnd: Long)
