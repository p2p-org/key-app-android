package org.p2p.token.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import org.p2p.token.service.model.TokenServiceNetwork

@Entity(
    tableName = TokenServicePriceEntity.TABLE_NAME
)
data class TokenServicePriceEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_TOKEN_ADDRESS)
    val tokenAddress: String,
    @ColumnInfo(name = COLUMN_TOKEN_PRICE)
    val price: BigDecimal?,
    @ColumnInfo(name = COLUMN_TOKEN_NETWORK_CHAIN)
    val network: TokenServiceNetwork
) {
    companion object {
        const val TABLE_NAME = "token_price_table"
        const val COLUMN_TOKEN_ADDRESS = "token_address"
        const val COLUMN_TOKEN_PRICE = "token_price"
        const val COLUMN_TOKEN_NETWORK_CHAIN = "token_network_chain"
    }
}
