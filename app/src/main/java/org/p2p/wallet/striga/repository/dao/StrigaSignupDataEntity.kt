package org.p2p.wallet.striga.repository.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import org.p2p.wallet.striga.repository.dao.StrigaSignupDataEntity.Companion.COLUMN_OWNER
import org.p2p.wallet.striga.repository.dao.StrigaSignupDataEntity.Companion.COLUMN_TYPE
import org.p2p.wallet.utils.Base58String

@Entity(
    tableName = "striga_signup_data",
    primaryKeys = [COLUMN_OWNER, COLUMN_TYPE],
    indices = [Index(COLUMN_OWNER)]
)
data class StrigaSignupDataEntity(
    // converts from textview tag
    @ColumnInfo(name = COLUMN_TYPE)
    val type: String,
    @ColumnInfo(name = COLUMN_VALUE)
    val value: String?,
    @ColumnInfo(name = COLUMN_OWNER)
    val ownerPublicKey: Base58String
) {
    companion object {
        const val COLUMN_TYPE = "type"
        const val COLUMN_VALUE = "value"
        const val COLUMN_OWNER = "owner"
    }

    fun belongsTo(user: Base58String): Boolean = ownerPublicKey == user
}
