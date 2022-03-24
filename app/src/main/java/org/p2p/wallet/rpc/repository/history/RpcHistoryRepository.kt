package org.p2p.wallet.rpc.repository.history

import org.p2p.solanaj.core.Transaction

interface RpcHistoryRepository {
    suspend fun sendTransaction(transaction: Transaction): String
    suspend fun simulateTransaction(transaction: Transaction): String
    suspend fun sendTransaction(serializedTransaction: String): String
    suspend fun simulateTransaction(serializedTransaction: String): String
}
