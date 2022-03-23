package org.p2p.wallet.infrastructure.db

import androidx.room.TypeConverter
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? =
        if (!value.isNullOrEmpty()) BigDecimal(value) else null

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? =
        value?.toString()

    @TypeConverter
    fun base58ToString(value: Base58String?): String? = value?.value

    @TypeConverter
    fun stringToBase58(value: String?): Base58String? = value?.let { Base58String(it) }
}
