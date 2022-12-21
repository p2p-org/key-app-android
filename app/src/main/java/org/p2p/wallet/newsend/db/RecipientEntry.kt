package org.p2p.wallet.newsend.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.home.db.TokenEntity.Companion.TABLE_NAME
import java.math.BigDecimal

@Entity(
    tableName = TABLE_NAME
)
data class RecipientEntry(

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

    @ColumnInfo(name = COLUMN_SERUM_V3_USDC)
    val serumV3Usdc: String?,

    @ColumnInfo(name = COLUMN_SERUM_V3_USDT)
    val serumV3Usdt: String?,

    @ColumnInfo(name = COLUMN_WRAPPED)
    val isWrapped: Boolean
) {
    companion object {
        const val TABLE_NAME = "recipient_table"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_DATE_TS = "date_timestamp"
    }
}
