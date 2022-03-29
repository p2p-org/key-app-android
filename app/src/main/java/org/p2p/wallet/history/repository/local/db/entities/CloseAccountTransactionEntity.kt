package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity

@Entity(tableName = CloseAccountTransactionEntity.TABLE_NAME)
class CloseAccountTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @ColumnInfo(name = COLUMN_ACCOUNT)
    val account: String?,

    @ColumnInfo(name = COLUMN_MINT)
    val mint: String?
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "close_account_transactions"

        const val COLUMN_ACCOUNT = "account"
        const val COLUMN_MINT = "mint"
    }
}
