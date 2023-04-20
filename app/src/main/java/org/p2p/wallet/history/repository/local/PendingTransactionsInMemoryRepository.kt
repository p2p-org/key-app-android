package org.p2p.wallet.history.repository.local

import org.p2p.core.utils.Constants
import org.p2p.wallet.history.model.HistoryPendingTransaction
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class PendingTransactionsInMemoryRepository : PendingTransactionsLocalRepository {
    private val pendingTransactions = mutableMapOf<Base58String, MutableList<HistoryPendingTransaction>>()

    override suspend fun savePendingTransaction(
        mintAddress: Base58String,
        txSignature: String,
        transaction: HistoryTransaction,
    ) {
        if (pendingTransactions[mintAddress] == null) {
            pendingTransactions[mintAddress] = mutableListOf()
        }
        pendingTransactions[mintAddress]?.add(HistoryPendingTransaction(txSignature, transaction))
    }

    override suspend fun getAllPendingTransactions(mintAddress: Base58String): List<HistoryTransaction> {
        return if (mintAddress == Constants.WRAPPED_SOL_MINT.toBase58Instance()) {
            pendingTransactions.values.flatten()
                .map { it.transaction }
        } else {
            pendingTransactions.getOrDefault(key = mintAddress, defaultValue = mutableListOf())
                .map { it.transaction }
        }
    }

    override suspend fun findPendingTransaction(txSignature: String): HistoryTransaction? {
        return pendingTransactions.values.flatten()
            .firstOrNull { it.txSignature == txSignature }
            ?.transaction
    }

    override suspend fun removePendingTransaction(txSignature: String) {
        pendingTransactions.keys.forEach { mintAddress ->
            val foundItem = pendingTransactions[mintAddress]?.firstOrNull { it.txSignature == txSignature }
            if (foundItem != null) {
                pendingTransactions[mintAddress]?.remove(foundItem)
            }
        }
    }
}
