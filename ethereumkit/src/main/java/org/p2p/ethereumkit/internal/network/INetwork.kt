package org.p2p.ethereumkit.internal.network

import org.p2p.ethereumkit.internal.spv.models.BlockHeader

interface INetwork {
    val id: Int
    val genesisBlockHash: ByteArray
    val checkpointBlock: BlockHeader
    val blockTime: Long
}
