package org.p2p.wallet.history.db.entities.embedded

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import org.p2p.wallet.utils.fromJsonReified
import java.util.*


@Entity
class CommonTransactionInformationEntity(
    @ColumnInfo(name = COLUMN_BLOCK_TIME)
    val blockTimeSec: Long,

    @ColumnInfo(name = COLUMN_TRANSACTION_TYPE)
    @TypeConverters(TransactionTypeEntity.Converter::class)
    val transactionDetailsType: TransactionTypeEntity,

    @ColumnInfo(name = COLUMN_INFO)
    val information: String?, // stored in json, cant store java.lang.Object here
) {
    companion object {
        const val COLUMN_BLOCK_TIME = "block_time"
        const val COLUMN_TRANSACTION_TYPE = "transaction_type"
        const val COLUMN_INFO = "info"
    }
}

