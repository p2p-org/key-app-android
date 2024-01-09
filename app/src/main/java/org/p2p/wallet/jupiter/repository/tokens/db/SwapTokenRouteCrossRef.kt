package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "swap_tokens_routes",
    primaryKeys = ["index_source_token", "index_swappable_token"],
)
data class SwapTokenRouteCrossRef(
    @ColumnInfo(name = "index_source_token")
    val indexOfSourceToken: Int,
    @ColumnInfo(name = "index_swappable_token")
    val swappableTokenIndex: Int
)
