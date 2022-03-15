package org.p2p.wallet.history.db.dao

import org.p2p.wallet.history.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.db.entities.TransactionEntity
import org.p2p.wallet.history.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.db.entities.UnknownTransactionEntity
import org.p2p.wallet.utils.findInstance
import timber.log.Timber

class TransactionDaoDelegate(
    private val transactionDao: List<TransactionDao<*>>
) {

    suspend fun getTransactions(signatures: List<String>): List<TransactionEntity> {
        return transactionDao
            .map { daoImpl -> daoImpl.getTransactionsBySignature(signatures) }
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

        Timber.i("Inserted entity: ${entity.commonInformation.transactionDetailsType}")
    }
}