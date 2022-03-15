package org.p2p.wallet.history.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import org.p2p.wallet.history.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.db.entities.TransactionEntity
import org.p2p.wallet.history.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.db.entities.UnknownTransactionEntity

@Dao
abstract class TransactionDao<TRANSACTION : TransactionEntity>(private val tableName: String) {
    @RawQuery
    abstract suspend fun getTransactionsByQuery(query: SupportSQLiteQuery): List<TRANSACTION>

    suspend fun getTransactionsBySignature(signatures: List<String>): List<TRANSACTION> {
        val signaturesListInSql = signatures.joinToString(separator = ",") { "\'$it\'" }
        return getTransactionsByQuery(
            SimpleSQLiteQuery(
                //language=RoomSql
                "SELECT * FROM $tableName WHERE signature in ($signaturesListInSql)"
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(entity: TRANSACTION)
}

@Dao
abstract class CreateAccountTransactionsDao :
    TransactionDao<CreateAccountTransactionEntity>(CreateAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class CloseAccountTransactionsDao :
    TransactionDao<CloseAccountTransactionEntity>(CloseAccountTransactionEntity.TABLE_NAME)

@Dao
abstract class SwapTransactionsDao :
    TransactionDao<SwapTransactionEntity>(SwapTransactionEntity.TABLE_NAME)

@Dao
abstract class TransferTransactionsDao :
    TransactionDao<TransferTransactionEntity>(TransferTransactionEntity.TABLE_NAME)

@Dao
abstract class RenBtcBurnOrMintTransactionsDao :
    TransactionDao<RenBtcBurnOrMintTransactionEntity>(RenBtcBurnOrMintTransactionEntity.TABLE_NAME)

@Dao
abstract class UnknownTransactionsDao :
    TransactionDao<UnknownTransactionEntity>(UnknownTransactionEntity.TABLE_NAME)