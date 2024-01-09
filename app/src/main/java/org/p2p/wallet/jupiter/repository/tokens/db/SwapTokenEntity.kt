package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swap_tokens")
data class SwapTokenEntity(
    @ColumnInfo(name = "ordinal_index")
    val ordinalIndex: Int,
    @PrimaryKey
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "chainId")
    val chainId: Int,
    @ColumnInfo(name = "decimals")
    val decimals: Int,
    @ColumnInfo(name = "logo_uri")
    val logoUri: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "ext_coingecko_id")
    val coingeckoId: String?,
)
