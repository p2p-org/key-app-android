package org.p2p.wallet.history.repository.local.db.entities.embedded

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters

@Entity
class CommonTransactionInformationEntity(
    @ColumnInfo(name = COLUMN_BLOCK_TIME)
    val blockTimeSec: Long,

    @ColumnInfo(name = COLUMN_TRANSACTION_TYPE)
    @TypeConverters(TransactionTypeEntity.Converter::class)
    val transactionDetailsType: TransactionTypeEntity,
) {
    companion object {
        const val COLUMN_BLOCK_TIME = "block_time"
        const val COLUMN_TRANSACTION_TYPE = "transaction_type"
    }
}
