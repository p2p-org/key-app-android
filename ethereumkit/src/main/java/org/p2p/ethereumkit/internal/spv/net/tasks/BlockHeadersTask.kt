package org.p2p.ethereumkit.internal.spv.net.tasks

import org.p2p.ethereumkit.internal.spv.core.ITask
import org.p2p.ethereumkit.internal.spv.models.BlockHeader

class BlockHeadersTask(val blockHeader: BlockHeader, val limit: Int, val reverse: Boolean = false) : ITask
