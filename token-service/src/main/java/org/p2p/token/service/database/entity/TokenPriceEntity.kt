package org.p2p.token.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import org.p2p.token.service.database.entity.TokenPriceEntity.Companion.TABLE_NAME
import org.p2p.token.service.model.TokenServiceNetwork

@Entity(tableName = TABLE_NAME)
data class TokenPriceEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_TOKEN_ADDRESS)
    val tokenAddress: String,
    @ColumnInfo(name = COLUMN_NETWORK_CHAIN)
    val networkChain: String,
    @Embedded
    val tokenRate: TokenRateEntity

) {
    companion object {
        const val TABLE_NAME = "token_price_table"
        const val COLUMN_TOKEN_ADDRESS = "token_address"
        const val COLUMN_NETWORK_CHAIN = "token_network_chain"
        const val COLUMN_TOKEN_RATES = "token_rates"
    }
}
