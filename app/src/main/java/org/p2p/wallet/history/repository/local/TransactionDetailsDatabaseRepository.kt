package org.p2p.wallet.history.repository.local

import org.p2p.core.utils.Constants
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.model.HistoryPendingTransaction
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDaoDelegate

class TransactionDetailsDatabaseRepository(
    private val daoDelegate: TransactionsDaoDelegate,
    private val mapper: TransactionDetailsEntityMapper,
) : TransactionDetailsLocalRepository {
    private val pendingTransactions = mutableMapOf<String, MutableList<HistoryPendingTransaction>>()

    override suspend fun getTransactions(signatures: List<String>): List<TransactionDetails> {
        return daoDelegate.getTransactions(signatures)
            .let { mapper.fromEntityToDomain(it) }
    }

    override suspend fun savePendingTransaction(
        mintAddress: String,
        txSignature: String,
        transaction: HistoryTransaction,
    ) {
        if (pendingTransactions[mintAddress] == null) {
            pendingTransactions[mintAddress] = mutableListOf()
        }
        pendingTransactions[mintAddress]?.add(HistoryPendingTransaction(txSignature, transaction))
    }

    override suspend fun getAllPendingTransactions(mintAddress: String): List<HistoryTransaction> {
        return if (mintAddress == Constants.WRAPPED_SOL_MINT) {
            pendingTransactions.values.flatten().map { it.transaction }
        } else {
            pendingTransactions.getOrPut(mintAddress, defaultValue = { mutableListOf() }).map { it.transaction }
        }
    }

    override suspend fun findPendingTransaction(txSignature: String): HistoryTransaction? {
        return pendingTransactions.values.flatten().firstOrNull { it.txSignature == txSignature }?.transaction
    }

    override suspend fun removePendingTransaction(txSignature: String) {
        pendingTransactions.keys.forEach { mintAddress ->
            val foundItem = pendingTransactions[mintAddress]?.firstOrNull { it.txSignature == txSignature }
            if (foundItem != null) {
                pendingTransactions[mintAddress]?.remove(foundItem)
            }
        }
    }

    override suspend fun saveTransactions(transactionDetails: List<TransactionDetails>) {
        daoDelegate.insertTransactions(
            entities = transactionDetails.let { mapper.fromDomainToEntity(it) }
        )
    }

    override suspend fun deleteAll() {
        daoDelegate.deleteAll()
    }
}
