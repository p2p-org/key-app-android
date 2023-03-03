package org.p2p.ethereumkit.internal.spv.net.tasks

import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.spv.core.ITask
import org.p2p.ethereumkit.internal.spv.models.BlockHeader

class AccountStateTask(val address: EthAddress, val blockHeader: BlockHeader) : ITask
