package org.p2p.wallet.rpc.repository.history

import org.p2p.solanaj.core.Transaction

/**
 * All transactions are being sent only in BASE64 encoding here
 * */
interface RpcTransactionRepository {
    suspend fun sendTransaction(transaction: Transaction): String
    suspend fun simulateTransaction(transaction: Transaction): String

    suspend fun sendTransaction(serializedTransaction: String): String
    suspend fun simulateTransaction(serializedTransaction: String): String
}
