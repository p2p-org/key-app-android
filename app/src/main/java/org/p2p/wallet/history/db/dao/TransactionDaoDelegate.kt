package org.p2p.wallet.history.db.dao

import org.p2p.wallet.history.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.db.entities.TransactionEntity
import org.p2p.wallet.history.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.db.entities.UnknownTransactionEntity
import org.p2p.wallet.utils.findInstance

class TransactionDaoDelegate(
    private val transactionDao: List<TransactionDao<*>>
) {

    fun getTransactions(signatures: List<String>): List<TransactionEntity> {
        return transactionDao.flatMap { daoImpl ->
            daoImpl.getTransactionsBySignature(signatures)
        }
    }

    fun insertTransactions(entities: List<TransactionEntity>) {
        entities.groupBy { it.javaClass }
            .forEach { (key: Class<TransactionEntity>, value: List<TransactionEntity>) ->
                insertEntityByClass(key, value)
            }
    }

    private fun insertEntityByClass(
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
        }
    }

    fun deleteAll() = transactionDao.forEach(TransactionDao<*>::deleteAll)
}