package org.p2p.solanaj.kits.renBridge

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.ByteUtils

private const val GATEWAY_REGISTRY_DATA_LENGTH =
    1 + PublicKey.PUBLIC_KEY_LENGTH +
        ByteUtils.UINT_64_LENGTH +
        ByteUtils.UINT_32_LENGTH +
        (32 * PublicKey.PUBLIC_KEY_LENGTH) +
        ByteUtils.UINT_32_LENGTH +
        (32 * PublicKey.PUBLIC_KEY_LENGTH)

class GatewayRegistryData(data: ByteArray) : AbstractData(data, GATEWAY_REGISTRY_DATA_LENGTH) {

    companion object {
        fun decode(data: ByteArray) = GatewayRegistryData(data)
    }

    val isInitialized: Boolean = readByte().toInt() != 0
    val owner: PublicKey = readPublicKey()
    val count: Int = readUint64().toInt()
    val selectorsSize = readUint32()
    val selectors = mutableListOf<String>()
    val gateways = mutableListOf<PublicKey>()

    init {

        for (i in 0 until selectorsSize) {
            val selector = readBytes(32)
            selectors.add(Base58.encode(selector))
        }

        val gatewaysSize = readUint32()

        for (i in 0 until gatewaysSize) {
            gateways.add(readPublicKey())
        }
    }
}
