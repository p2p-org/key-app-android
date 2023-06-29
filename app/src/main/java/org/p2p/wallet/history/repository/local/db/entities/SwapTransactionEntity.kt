package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity
import org.p2p.core.crypto.Base58String

@Entity(tableName = SwapTransactionEntity.TABLE_NAME)
class SwapTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @Embedded
    val aEntity: SwapAEntity,

    @Embedded
    val bEntity: SwapBEntity,

    @ColumnInfo(name = COLUMN_FEE)
    val fee: Long

) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "swap_transactions"

        const val COLUMN_FEE = "fee"
    }
}

@Entity
class SwapAEntity(
    @ColumnInfo(name = COLUMN_MINT_A)
    val mint: String?,

    @ColumnInfo(name = COLUMN_AMOUNT_A)
    val amount: String,

    @ColumnInfo(name = COLUMN_SOURCE)
    val source: Base58String?,

    @ColumnInfo(name = COLUMN_SOURCE_ALT)
    val alternateSource: Base58String?
) {
    companion object {
        const val COLUMN_MINT_A = "mint_a"
        const val COLUMN_AMOUNT_A = "amount_a"
        const val COLUMN_SOURCE = "source"
        const val COLUMN_SOURCE_ALT = "alt_source"
    }
}

@Entity
class SwapBEntity(
    @ColumnInfo(name = COLUMN_MINT_B)
    val mint: String?,

    @ColumnInfo(name = COLUMN_AMOUNT_B)
    val amount: String,

    @ColumnInfo(name = COLUMN_DESTINATION)
    val destination: Base58String?,

    @ColumnInfo(name = COLUMN_DESTINATION_ALT)
    val alternateDestination: Base58String?
) {
    companion object {
        const val COLUMN_MINT_B = "mint_b"
        const val COLUMN_AMOUNT_B = "amount_b"
        const val COLUMN_DESTINATION = "destination"
        const val COLUMN_DESTINATION_ALT = "alt_destination"
    }
}
