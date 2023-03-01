package org.p2p.ethereumkit.spv.net

import org.p2p.ethereumkit.core.ISpvStorage
import org.p2p.ethereumkit.network.INetwork
import org.p2p.ethereumkit.spv.models.BlockHeader

class BlockHelper(val storage: ISpvStorage, val network: INetwork) {

    val lastBlockHeader: BlockHeader
        get() = storage.getLastBlockHeader() ?: network.checkpointBlock

}
