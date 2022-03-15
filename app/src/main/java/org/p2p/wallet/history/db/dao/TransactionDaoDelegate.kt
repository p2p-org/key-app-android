package org.p2p.wallet.history.db.dao

import org.p2p.wallet.history.db.entities.*
import org.p2p.wallet.utils.findInstance

class TransactionDaoDelegate(
    private val transactionDao: List<TransactionDao<*>>,
) {

    suspend fun getTransactions(): List<TransactionEntity> {
        return transactionDao
            .map { daoImpl -> daoImpl.getAllTransactions() }
            .flatten()
    }

    suspend fun insertTransaction(entity: TransactionEntity) {
        insertEntity(entity)
    }

    private suspend fun insertEntity(entity: TransactionEntity) {
        when (entity) {
            is CreateAccountTransactionEntity -> {
                transactionDao.findInstance<CreateAccountTransactionsDao>()
                    ?.insertTransaction(entity)
            }
            is CloseAccountTransactionEntity -> {
                transactionDao.findInstance<CloseAccountTransactionsDao>()
                    ?.insertTransaction(entity)
            }
            is RenBtcBurnOrMintTransactionEntity -> {
                transactionDao.findInstance<RenBtcBurnOrMintTransactionsDao>()
                    ?.insertTransaction(entity)
            }
            is SwapTransactionEntity -> {
                transactionDao.findInstance<SwapTransactionsDao>()
                    ?.insertTransaction(entity)
            }
            is TransferTransactionEntity -> {
                transactionDao.findInstance<TransferTransactionsDao>()
                    ?.insertTransaction(entity)
            }
            is UnknownTransactionEntity -> {
                transactionDao.findInstance<UnknownTransactionsDao>()
                    ?.insertTransaction(entity)
            }
        }
    }
}