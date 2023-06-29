package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity
import org.p2p.core.crypto.Base58String

@Entity(tableName = TransferTransactionEntity.TABLE_NAME)
class TransferTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @ColumnInfo(name = COLUMN_SOURCE)
    val source: Base58String?,

    @ColumnInfo(name = COLUMN_DESTINATION)
    val destination: Base58String?,

    @ColumnInfo(name = COLUMN_AUTHORITY)
    val authority: String?,

    @ColumnInfo(name = COLUMN_MINT)
    val mint: String?,

    @ColumnInfo(name = COLUMN_AMOUNT)
    val amount: String?,

    @ColumnInfo(name = COLUMN_DECIMALS)
    val decimals: Int,

    @ColumnInfo(name = COLUMN_FEE)
    val fee: Long,

    @ColumnInfo(name = COLUMN_PROGRAM_ID)
    val programId: String
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "transfer_transactions"

        const val COLUMN_SOURCE = "source"
        const val COLUMN_DESTINATION = "destination"
        const val COLUMN_AUTHORITY = "authority"
        const val COLUMN_MINT = "mint"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DECIMALS = "decimals"
        const val COLUMN_FEE = "fee"
        const val COLUMN_PROGRAM_ID = "program_id"
    }
}
