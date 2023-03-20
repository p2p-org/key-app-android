package org.p2p.wallet.history.repository.local

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDaoDelegate

class TransactionDetailsDatabaseRepository(
    private val daoDelegate: TransactionsDaoDelegate,
    private val mapper: TransactionDetailsEntityMapper,
) : TransactionDetailsLocalRepository {
    private val pendingTransactions = HashMap<String, HistoryTransaction?>()

    override suspend fun getTransactions(signatures: List<String>): List<TransactionDetails> {
        return daoDelegate.getTransactions(signatures)
            .let { mapper.fromEntityToDomain(it) }
    }

    override suspend fun savePendingTransaction(txSignature: String, transaction: HistoryTransaction) {
        pendingTransactions[txSignature] = transaction
    }

    override suspend fun getAllPendingTransactions(): List<HistoryTransaction> {
        return pendingTransactions.values.filterNotNull()
    }

    override suspend fun findPendingTransaction(txSignature: String): HistoryTransaction? {
        return pendingTransactions[txSignature]
    }

    override suspend fun removePendingTransaction(txSignature: String) {
        pendingTransactions[txSignature] = null
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
