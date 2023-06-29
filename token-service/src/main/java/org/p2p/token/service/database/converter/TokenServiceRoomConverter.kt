package org.p2p.token.service.database.converter

import androidx.room.TypeConverter
import org.p2p.token.service.model.TokenServiceNetwork

internal object  TokenServiceRoomConverter {
    @TypeConverter
    fun tokenServiceNetworkToString(value: TokenServiceNetwork?) = value?.networkName

    @TypeConverter
    fun stringToTokenServiceNetwork(value: String) = TokenServiceNetwork.valueOf(value)
}
