package org.p2p.solanaj.kits.renBridge

import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

private const val GATEWAY_STATE_DATA_LENGTH = 1 + 20 + 32 + ByteUtils.UINT_64_LENGTH + 1

class GatewayStateData(data: ByteArray) : AbstractData(data, GATEWAY_STATE_DATA_LENGTH) {

    val isInitialized: Boolean = readByte().toInt() != 0
    val renVMAuthority: ByteArray = readBytes(20)
    private val selectors: ByteArray = readBytes(32)
    val burnCount: BigInteger = readUint64()
    private val underlyingDecimals: Byte = readByte()

    companion object {
        fun decode(data: ByteArray): GatewayStateData = GatewayStateData(data)
    }
}
