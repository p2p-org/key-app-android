package org.p2p.wallet.history.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import org.p2p.wallet.history.db.entities.*
import org.p2p.wallet.history.db.entities.embedded.TransactionTypeEntity

@Dao
abstract class TransactionDao<TRANSACTION : TransactionEntity>(private val tableName: String) {
    @RawQuery
    abstract suspend fun getTransactionsByQuery(query: SupportSQLiteQuery): List<TRANSACTION>

    suspend fun getAllTransactions(): List<TRANSACTION> {
        return getTransactionsByQuery(SimpleSQLiteQuery("SELECT * FROM $tableName"))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(entity: TRANSACTION)

    class TransactionDaoSelectQuery(private val entityType: TransactionTypeEntity) {
        val sqliteQuery: SupportSQLiteQuery
            get() = createQuery()


        private fun createQuery(): SimpleSQLiteQuery {
            val tableName = when (entityType) {
                TransactionTypeEntity.CLOSE_ACCOUNT -> CloseAccountTransactionEntity.TABLE_NAME
                TransactionTypeEntity.CREATE_ACCOUNT -> CreateAccountTransactionEntity.TABLE_NAME
                TransactionTypeEntity.SWAP -> SwapTransactionEntity.TABLE_NAME
                TransactionTypeEntity.TRANSFER -> TransferTransactionEntity.TABLE_NAME
                TransactionTypeEntity.REN_BTC_TRANSFER -> RenBtcBurnOrMintTransactionEntity.TABLE_NAME
                TransactionTypeEntity.UNKNOWN -> UnknownTransactionEntity.TABLE_NAME
            }

            return SimpleSQLiteQuery("SELECT * FROM $tableName")
        }
    }
}

@Dao
abstract class CreateAccountTransactionsDao : TransactionDao<CreateAccountTransactionEntity>(CreateAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class CloseAccountTransactionsDao : TransactionDao<CloseAccountTransactionEntity>(CloseAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class SwapTransactionsDao : TransactionDao<SwapTransactionEntity>(SwapTransactionEntity.TABLE_NAME)

@Dao
abstract class TransferTransactionsDao : TransactionDao<TransferTransactionEntity>(TransferTransactionEntity.TABLE_NAME)

@Dao
abstract class RenBtcBurnOrMintTransactionsDao : TransactionDao<RenBtcBurnOrMintTransactionEntity>(RenBtcBurnOrMintTransactionEntity.TABLE_NAME)

@Dao
abstract class UnknownTransactionsDao : TransactionDao<UnknownTransactionEntity>(UnknownTransactionEntity.TABLE_NAME)


