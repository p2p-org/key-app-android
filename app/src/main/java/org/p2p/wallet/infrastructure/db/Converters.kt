package org.p2p.wallet.infrastructure.db

import androidx.room.TypeConverter
import org.p2p.solanaj.utils.crypto.Base64String
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
    fun base58ToString(value: Base58String?): String? = value?.base58Value

    @TypeConverter
    fun stringToBase58(value: String?): Base58String? = value?.let { Base58String(it) }

    @TypeConverter
    fun base64ToString(value: Base64String?): String? = value?.base64Value

    @TypeConverter
    fun stringToBase64(value: String?): Base64String? = value?.let { Base64String(it) }
}
