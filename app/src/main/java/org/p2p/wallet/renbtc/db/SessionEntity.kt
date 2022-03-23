package org.p2p.wallet.renbtc.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = SessionEntity.TABLE_NAME
)
data class SessionEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_DESTINATION_ADDRESS)
    val destinationAddress: String,

    @ColumnInfo(name = COLUMN_NONCE)
    val nonce: String,

    @ColumnInfo(name = COLUMN_CREATED_AT)
    val createdAt: Long,

    @ColumnInfo(name = COLUMN_EXPIRY_TIME)
    val expiryTime: Long,

    @ColumnInfo(name = COLUMN_GATEWAY_ADDRESS)
    val gatewayAddress: String,

    @ColumnInfo(name = COLUMN_FEE)
    val fee: String
) {

    companion object {
        const val TABLE_NAME = "session_table"
        const val COLUMN_DESTINATION_ADDRESS = "destination_address"
        const val COLUMN_NONCE = "nonce"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_EXPIRY_TIME = "expiry_time"
        const val COLUMN_GATEWAY_ADDRESS = "gateway_address"
        const val COLUMN_FEE = "fee"
    }
}
