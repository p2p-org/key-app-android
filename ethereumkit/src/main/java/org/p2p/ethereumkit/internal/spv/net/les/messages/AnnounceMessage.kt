package org.p2p.ethereumkit.internal.spv.net.les.messages

import org.p2p.ethereumkit.internal.core.toHexString
import org.p2p.ethereumkit.internal.spv.core.toBigInteger
import org.p2p.ethereumkit.internal.spv.core.toLong
import org.p2p.ethereumkit.internal.spv.net.IInMessage
import org.p2p.core.wrapper.eth.rlp.RLP
import org.p2p.core.wrapper.eth.rlp.RLPList
import java.math.BigInteger

class AnnounceMessage(payload: ByteArray) : IInMessage {

    val blockHash: ByteArray
    val blockHeight: Long
    val blockTotalDifficulty: BigInteger
    val reorganizationDepth: Long

    init {
        val params = RLP.decode2(payload)[0] as RLPList
        blockHash = params[0].rlpData ?: byteArrayOf()
        blockHeight = params[1].rlpData.toLong()
        blockTotalDifficulty = params[2].rlpData.toBigInteger()
        reorganizationDepth = params[3].rlpData.toLong()
    }

    override fun toString(): String {
        return "Announce [blockHash: ${blockHash.toHexString()}; " +
                "blockHeight: $blockHeight; " +
                "blockTotalDifficulty: $blockTotalDifficulty; " +
                "reorganizationDepth: $reorganizationDepth]"
    }
}
