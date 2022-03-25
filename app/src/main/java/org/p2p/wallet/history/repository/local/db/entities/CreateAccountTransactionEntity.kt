package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity

@Entity(tableName = CreateAccountTransactionEntity.TABLE_NAME)
class CreateAccountTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,

    @ColumnInfo(name = COLUMN_FEE)
    val fee: Long
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "create_account_transactions"

        const val COLUMN_FEE = "fee"
    }
}
