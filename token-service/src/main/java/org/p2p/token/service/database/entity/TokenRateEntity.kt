package org.p2p.token.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
data class TokenRateEntity(
    @PrimaryKey
    val tokenAddress: String,
    @ColumnInfo(name = COLUMN_USD)
    val usd: BigDecimal
) {

    companion object {
        const val COLUMN_USD = "usd"
    }
}
