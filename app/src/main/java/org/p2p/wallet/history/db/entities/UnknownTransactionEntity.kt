package org.p2p.wallet.history.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.db.entities.embedded.TransactionIdentifiersEntity

@Entity(tableName = UnknownTransactionEntity.TABLE_NAME)
class UnknownTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @ColumnInfo(name = COLUMN_RAW_DATA)
    val rawData: String, // stored in json, cant store java.lang.Object

    @ColumnInfo(name = COLUMN_DATA)
    val data: String,
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "unknown_transactions"

        const val COLUMN_RAW_DATA = "raw_data"
        const val COLUMN_DATA = "data"
    }
}