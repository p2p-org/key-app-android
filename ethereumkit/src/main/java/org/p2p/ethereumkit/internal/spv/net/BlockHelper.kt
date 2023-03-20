package org.p2p.ethereumkit.internal.spv.net

import org.p2p.ethereumkit.internal.core.ISpvStorage
import org.p2p.ethereumkit.internal.network.INetwork
import org.p2p.ethereumkit.internal.spv.models.BlockHeader

class BlockHelper(val storage: ISpvStorage, val network: INetwork) {

    val lastBlockHeader: BlockHeader
        get() = storage.getLastBlockHeader() ?: network.checkpointBlock

}
