package org.p2p.wallet.history.repository.local

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDaoDelegate
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper

class TransactionDetailsDatabaseRepository(
    private val daoDelegate: TransactionsDaoDelegate,
    private val mapper: TransactionDetailsEntityMapper,
) : TransactionDetailsLocalRepository {

    override suspend fun getTransactions(signatures: List<String>): List<TransactionDetails> {
        return daoDelegate.getTransactions(signatures)
            .let { mapper.fromEntityToDomain(it) }
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
