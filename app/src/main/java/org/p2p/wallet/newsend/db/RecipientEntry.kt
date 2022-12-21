package org.p2p.wallet.newsend.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.newsend.db.RecipientEntry.Companion.TABLE_NAME

@Entity(
    tableName = TABLE_NAME
)
data class RecipientEntry(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ADDRESS)
    val address: String,

    @ColumnInfo(name = COLUMN_NICKNAME)
    val nickname: String?,

    @ColumnInfo(name = COLUMN_DATE_TIMESTAMP)
    val dateTimestamp: Long

) {
    companion object {
        const val TABLE_NAME = "recipient_table"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_DATE_TIMESTAMP = "date_timestamp"
    }
}
