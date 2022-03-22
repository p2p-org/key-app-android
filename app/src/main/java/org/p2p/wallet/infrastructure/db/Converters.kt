package org.p2p.wallet.infrastructure.db

import androidx.room.TypeConverter
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? =
        if (!value.isNullOrEmpty()) BigDecimal(value) else null

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? =
        value?.toString()
}
