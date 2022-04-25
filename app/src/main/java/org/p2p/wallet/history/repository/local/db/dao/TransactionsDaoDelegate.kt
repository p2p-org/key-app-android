package org.p2p.wallet.history.repository.local.db.dao

import org.p2p.wallet.history.repository.local.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.UnknownTransactionEntity
import org.p2p.wallet.utils.findInstance
import timber.log.Timber

class TransactionsDaoDelegate(
    private val transactionDao: List<TransactionsDao<*>>
) {

    suspend fun getTransactions(signatures: List<String>): List<TransactionEntity> {
        return transactionDao.flatMap { daoImpl ->
            daoImpl.getTransactionsBySignature(signatures)
        }
    }

    suspend fun insertTransactions(entities: List<TransactionEntity>) {
        entities.groupBy { it.javaClass }
            .forEach { (key: Class<TransactionEntity>, value: List<TransactionEntity>) ->
                insertEntityByClass(key, value)
            }
    }

    private suspend fun insertEntityByClass(
        entityClass: Class<TransactionEntity>,
        entitiesList: List<TransactionEntity>
    ) {
        when (entityClass) {
            CreateAccountTransactionEntity::class.java -> {
                transactionDao.findInstance<CreateAccountTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<CreateAccountTransactionEntity>())
            }
            CloseAccountTransactionEntity::class.java -> {
                transactionDao.findInstance<CloseAccountTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<CloseAccountTransactionEntity>())
            }
            RenBtcBurnOrMintTransactionEntity::class.java -> {
                transactionDao.findInstance<RenBtcBurnOrMintTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<RenBtcBurnOrMintTransactionEntity>())
            }
            SwapTransactionEntity::class.java -> {
                transactionDao.findInstance<SwapTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<SwapTransactionEntity>())
            }
            TransferTransactionEntity::class.java -> {
                transactionDao.findInstance<TransferTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<TransferTransactionEntity>())
            }
            UnknownTransactionEntity::class.java -> {
                transactionDao.findInstance<UnknownTransactionsDao>()
                    ?.insertTransactions(entitiesList.filterIsInstance<UnknownTransactionEntity>())
            }
            else -> null
        }
            ?.also { Timber.d("${entityClass.simpleName} inserted: size=${it.size}") }
    }

    suspend fun deleteAll() {
        transactionDao.forEach { it.deleteAll() }
    }
}
