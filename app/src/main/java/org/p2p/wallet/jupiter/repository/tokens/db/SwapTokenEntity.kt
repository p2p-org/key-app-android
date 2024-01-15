package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// if it works slow - try to use @DatabaseView
@Entity(
    tableName = "swap_tokens",
    indices = [Index("symbol"), Index("address")]
)
data class SwapTokenEntity(
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
    @ColumnInfo(name = "tags")
    val tagsAsJsonList: String,
    @ColumnInfo(name = "ext_coingecko_id")
    val coingeckoId: String?,
)
