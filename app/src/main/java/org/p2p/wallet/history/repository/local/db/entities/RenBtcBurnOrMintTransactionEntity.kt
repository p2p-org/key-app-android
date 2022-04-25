package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity

@Entity(tableName = RenBtcBurnOrMintTransactionEntity.TABLE_NAME)
class RenBtcBurnOrMintTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @ColumnInfo(name = COLUMN_ACCOUNT)
    val account: String?,

    @ColumnInfo(name = COLUMN_AUTHORITY)
    val authority: String?,

    @ColumnInfo(name = COLUMN_AMOUNT)
    val amount: String?,

    @ColumnInfo(name = COLUMN_DECIMALS)
    val decimals: Int,

    @ColumnInfo(name = COLUMN_FEE)
    val fee: Long
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "burn_or_mint_transactions"

        const val COLUMN_ACCOUNT = "account"
        const val COLUMN_AUTHORITY = "authority"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DECIMALS = "decimals"
        const val COLUMN_FEE = "fee"
    }
}
