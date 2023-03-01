package org.p2p.ethereumkit.api.storage

import androidx.room.TypeConverter
import org.p2p.ethereumkit.models.EthAddress
import java.math.BigInteger

class RoomTypeConverters {
    @TypeConverter
    fun bigIntegerFromString(string: String?): BigInteger? {
        return string?.let { BigInteger(it) }
    }

    @TypeConverter
    fun bigIntegerToString(bigInteger: BigInteger?): String? {
        return bigInteger?.toString()
    }

    @TypeConverter
    fun addressFromByteArray(rawAddress: ByteArray?): EthAddress? {
        return rawAddress?.let { EthAddress(it) }
    }

    @TypeConverter
    fun addressToByteArray(address: EthAddress?): ByteArray? {
        return address?.raw
    }
}
