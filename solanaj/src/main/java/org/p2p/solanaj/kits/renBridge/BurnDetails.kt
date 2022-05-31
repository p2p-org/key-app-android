package org.p2p.solanaj.kits.renBridge

import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

private const val GATEWAY_STATE_DATA_LENGTH = 1 + 20 + 32 + ByteUtils.UINT_64_LENGTH + 1

class BurnDetails {

    var confirmedSignature: String = ""
    var nonce: BigInteger = BigInteger.ZERO
    var recepient: String = ""

    class GatewayStateData(private val data: ByteArray) : AbstractData(data, GATEWAY_STATE_DATA_LENGTH) {

        companion object {
            fun decode(data: ByteArray) = GatewayStateData(data)
        }

        val isInitialized: Boolean
            get() = readByte().toInt() != 0

        val renVMAuthority: ByteArray
            get() = readBytes(20)

        private val selectors: ByteArray
            get() = readBytes(32)

        val burnCount: BigInteger
            get() = readUint64()

        private val underlyingDecimals: Int
            get() = readByte().toInt()
    }
}
