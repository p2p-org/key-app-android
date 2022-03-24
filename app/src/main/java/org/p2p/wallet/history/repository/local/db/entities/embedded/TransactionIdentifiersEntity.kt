package org.p2p.wallet.history.repository.local.db.entities.embedded

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Primary keys for all transactions in database
 * for usage add field like this in your transaction entity:
 * @Embedded
 * @PrimaryKey <-- important!
 * val identifiers: TransactionIdentifierEntity
 */
@Entity
class TransactionIdentifiersEntity(
    @ColumnInfo(name = COLUMN_SIGNATURE)
    val signature: String,

    @ColumnInfo(name = COLUMN_SLOT)
    val blockId: Int,
) {
    companion object {
        const val COLUMN_SIGNATURE = "signature"
        const val COLUMN_SLOT = "slot"
    }
}
