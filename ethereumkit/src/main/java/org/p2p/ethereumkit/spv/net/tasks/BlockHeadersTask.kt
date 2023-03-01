package org.p2p.ethereumkit.spv.net.tasks

import org.p2p.ethereumkit.spv.core.ITask
import org.p2p.ethereumkit.spv.models.BlockHeader

class BlockHeadersTask(val blockHeader: BlockHeader, val limit: Int, val reverse: Boolean = false) : ITask
