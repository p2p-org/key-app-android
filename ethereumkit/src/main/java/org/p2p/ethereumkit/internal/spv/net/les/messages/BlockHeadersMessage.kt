package org.p2p.ethereumkit.internal.spv.net.les.messages

import org.p2p.ethereumkit.internal.spv.core.toLong
import org.p2p.ethereumkit.internal.spv.models.BlockHeader
import org.p2p.ethereumkit.internal.spv.net.IInMessage
import org.p2p.core.wrapper.eth.rlp.RLP
import org.p2p.core.wrapper.eth.rlp.RLPList

class BlockHeadersMessage(payload: ByteArray) : IInMessage {

    val requestID: Long
    val bv: Long
    val headers: List<BlockHeader>

    init {
        val headers = mutableListOf<BlockHeader>()
        val paramsList = RLP.decode2(payload)[0] as RLPList
        this.requestID = paramsList[0].rlpData.toLong()
        this.bv = paramsList[1].rlpData.toLong()
        val payloadList = paramsList[2] as RLPList
        for (i in 0 until payloadList.size) {
            val rlpData = payloadList[i] as RLPList
            headers.add(BlockHeader(rlpData))
        }
        this.headers = headers
    }

    override fun toString(): String {
        return "Headers [requestId: $requestID; bv: $bv; headers (${headers.size}): [${headers.joinToString(separator = ", ")}] ]"
    }
}
