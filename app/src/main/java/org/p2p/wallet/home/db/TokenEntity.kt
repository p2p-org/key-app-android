package org.p2p.wallet.home.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import org.p2p.wallet.home.db.TokenEntity.Companion.TABLE_NAME

@Entity(
    tableName = TABLE_NAME
)
data class TokenEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_PUBLIC_KEY)
    val publicKey: String,

    @ColumnInfo(name = COLUMN_SYMBOL)
    val tokenSymbol: String,

    @ColumnInfo(name = COLUMN_DECIMALS)
    val decimals: Int,

    @ColumnInfo(name = COLUMN_MINT_ADDRESS)
    val mintAddress: String,

    @ColumnInfo(name = COLUMN_TOKEN_NAME)
    val tokenName: String,

    @ColumnInfo(name = COLUMN_ICON_URL)
    val iconUrl: String?,

    @ColumnInfo(name = COLUMN_PRICE)
    val totalInUsd: BigDecimal?,

    @ColumnInfo(name = COLUMN_TOTAL)
    val total: BigDecimal,

    @ColumnInfo(name = COLUMN_EXCHANGE_RATE)
    val exchangeRate: String?,

    @ColumnInfo(name = COLUMN_VISIBILITY)
    val visibility: String,

    @ColumnInfo(name = COLUMN_WRAPPED)
    val isWrapped: Boolean,

    @ColumnInfo(name = COLUMN_TOKEN_SERVICE_ADDRESS)
    val tokenServiceAddress: String,

    @Embedded(prefix = COLUMN_EXTENSIONS)
    val extensions: TokenExtensionEntity?
) {
    companion object {
        const val TABLE_NAME = "token_table"
        const val COLUMN_PUBLIC_KEY = "public_key"
        const val COLUMN_SYMBOL = "symbol"
        const val COLUMN_DECIMALS = "decimals"
        const val COLUMN_MINT_ADDRESS = "mint_address"
        const val COLUMN_TOKEN_NAME = "token_name"
        const val COLUMN_ICON_URL = "icon_url"
        const val COLUMN_PRICE = "price"
        const val COLUMN_TOTAL = "total"
        const val COLUMN_EXCHANGE_RATE = "exchange_rate"
        const val COLUMN_VISIBILITY = "visibility"
        const val COLUMN_WRAPPED = "wrapped"
        const val COLUMN_TOKEN_SERVICE_ADDRESS = "token_service_address"
        const val COLUMN_EXTENSIONS = "ext_"
    }
}
