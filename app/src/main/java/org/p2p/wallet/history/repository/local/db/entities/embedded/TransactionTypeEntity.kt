package org.p2p.wallet.history.repository.local.db.entities.embedded

import androidx.room.TypeConverter

enum class TransactionTypeEntity(val typeStr: String) {
    UNKNOWN("unknown"),
    SWAP("swap"),
    TRANSFER("transfer"),
    TRANSFER_CHECKED("transferChecked"),
    REN_BTC_TRANSFER("transfer"),
    CREATE_ACCOUNT("create"),
    CLOSE_ACCOUNT("closeAccount");

    object Converter {
        @TypeConverter
        fun toEntity(typeStr: String): TransactionTypeEntity = enumValueOf(typeStr)

        @TypeConverter
        fun fromEntity(value: TransactionTypeEntity): String = value.typeStr
    }
}
