package org.p2p.ethereumkit.internal.spv.net.tasks

import org.p2p.ethereumkit.internal.network.INetwork
import org.p2p.ethereumkit.internal.spv.core.ITask
import org.p2p.ethereumkit.internal.spv.models.BlockHeader
import java.math.BigInteger

class HandshakeTask(val peerId: String, network: INetwork, blockHeader: BlockHeader) : ITask {
    val networkId: Int = network.id
    val genesisHash: ByteArray = network.genesisBlockHash
    val headTotalDifficulty: BigInteger = blockHeader.totalDifficulty
    val headHash: ByteArray = blockHeader.hashHex
    val headHeight: Long = blockHeader.height
}
