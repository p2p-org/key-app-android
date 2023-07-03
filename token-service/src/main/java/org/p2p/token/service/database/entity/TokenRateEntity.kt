package org.p2p.token.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

data class TokenRateEntity(
    @ColumnInfo(name = COLUMN_USD)
    val usd: BigDecimal?
) {
    companion object {
        const val COLUMN_USD = "usd"
    }
}
