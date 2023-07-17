package org.p2p.token.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import org.p2p.token.service.database.entity.TokenServicePriceEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
internal data class TokenServicePriceEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_TOKEN_ADDRESS)
    val tokenAddress: String,
    @ColumnInfo(name = COLUMN_NETWORK_CHAIN)
    val networkChainName: String,
    @ColumnInfo(name = COLUMN_USD_RATE)
    val usdRate: BigDecimal
) {
    companion object {
        const val TABLE_NAME = "token_price_table"
        const val COLUMN_TOKEN_ADDRESS = "token_address"
        const val COLUMN_NETWORK_CHAIN = "token_network_chain"
        const val COLUMN_USD_RATE = "token_usd_rate"
    }
}
