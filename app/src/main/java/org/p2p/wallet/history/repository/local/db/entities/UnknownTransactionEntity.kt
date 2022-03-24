package org.p2p.wallet.history.repository.local.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity

@Entity(tableName = UnknownTransactionEntity.TABLE_NAME)
class UnknownTransactionEntity(
    @Embedded
    @PrimaryKey
    val identifiers: TransactionIdentifiersEntity,

    @Embedded
    override val commonInformation: CommonTransactionInformationEntity,
) : TransactionEntity {
    companion object {
        const val TABLE_NAME = "unknown_transactions"
    }
}
