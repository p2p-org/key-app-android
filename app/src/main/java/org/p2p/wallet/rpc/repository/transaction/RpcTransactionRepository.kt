package org.p2p.wallet.rpc.repository.transaction

import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed

interface RpcTransactionRepository {
    suspend fun sendTransaction(transaction: Transaction): String
    suspend fun simulateTransaction(transaction: Transaction): String
    suspend fun sendTransaction(serializedTransaction: String): String
    suspend fun simulateTransaction(serializedTransaction: String): String
    suspend fun getConfirmedTransactions(signatures: List<String>): List<ConfirmedTransactionParsed>
}