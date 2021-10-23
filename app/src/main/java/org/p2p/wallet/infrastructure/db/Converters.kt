package org.p2p.wallet.infrastructure.db

import androidx.room.TypeConverter
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun stringToBigDecimal(value: String): BigDecimal =
        if (value.isEmpty()) BigDecimal.ZERO else BigDecimal(value)

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal): String =
        value.toString()
}