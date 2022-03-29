package org.p2p.wallet.history.repository.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import org.p2p.wallet.history.repository.local.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.UnknownTransactionEntity

@Dao
abstract class TransactionsDao<TRANSACTION : TransactionEntity>(private val tableName: String) {
    @RawQuery
    protected abstract suspend fun getTransactionsByQuery(query: SupportSQLiteQuery): List<TRANSACTION>

    suspend fun getTransactionsBySignature(signatures: List<String>): List<TRANSACTION> {
        val signaturesListInSql = signatures.joinToString(separator = ",") { "\'$it\'" }
        return getTransactionsByQuery(
            //language=RoomSql
            SimpleSQLiteQuery("SELECT * FROM $tableName WHERE signature in ($signaturesListInSql)")
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransactions(entities: List<TRANSACTION>): List<Long>

    @RawQuery
    protected abstract suspend fun deleteAll(query: SupportSQLiteQuery): Int

    //language=RoomSql
    suspend fun deleteAll() {
        deleteAll(SimpleSQLiteQuery("DELETE FROM $tableName"))
    }
}

@Dao
abstract class CreateAccountTransactionsDao :
    TransactionsDao<CreateAccountTransactionEntity>(CreateAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class CloseAccountTransactionsDao :
    TransactionsDao<CloseAccountTransactionEntity>(CloseAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class SwapTransactionsDao :
    TransactionsDao<SwapTransactionEntity>(SwapTransactionEntity.TABLE_NAME)

@Dao
abstract class TransferTransactionsDao :
    TransactionsDao<TransferTransactionEntity>(TransferTransactionEntity.TABLE_NAME)

@Dao
abstract class RenBtcBurnOrMintTransactionsDao :
    TransactionsDao<RenBtcBurnOrMintTransactionEntity>(RenBtcBurnOrMintTransactionEntity.TABLE_NAME)

@Dao
abstract class UnknownTransactionsDao :
    TransactionsDao<UnknownTransactionEntity>(UnknownTransactionEntity.TABLE_NAME)
