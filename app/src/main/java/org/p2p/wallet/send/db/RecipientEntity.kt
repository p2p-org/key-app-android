package org.p2p.wallet.send.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.send.db.RecipientEntity.Companion.TABLE_NAME

@Entity(
    tableName = TABLE_NAME
)
data class RecipientEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ADDRESS)
    val address: String,

    @ColumnInfo(name = COLUMN_NICKNAME)
    val username: String?,

    @ColumnInfo(name = COLUMN_DATE_TIMESTAMP)
    val dateTimestamp: Long,

    @ColumnInfo(name = COLUMN_NETWORK_TYPE)
    val networkTypeName: String

) {
    companion object {
        const val TABLE_NAME = "recipient_table"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_DATE_TIMESTAMP = "date_timestamp"
        const val COLUMN_NETWORK_TYPE = "network_type"
    }
}
