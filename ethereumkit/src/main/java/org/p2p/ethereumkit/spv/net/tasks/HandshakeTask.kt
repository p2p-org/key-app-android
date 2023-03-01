package org.p2p.ethereumkit.spv.net.tasks

import org.p2p.ethereumkit.network.INetwork
import org.p2p.ethereumkit.spv.core.ITask
import org.p2p.ethereumkit.spv.models.BlockHeader
import java.math.BigInteger

class HandshakeTask(val peerId: String, network: INetwork, blockHeader: BlockHeader) : ITask {
    val networkId: Int = network.id
    val genesisHash: ByteArray = network.genesisBlockHash
    val headTotalDifficulty: BigInteger = blockHeader.totalDifficulty
    val headHash: ByteArray = blockHeader.hashHex
    val headHeight: Long = blockHeader.height
}
