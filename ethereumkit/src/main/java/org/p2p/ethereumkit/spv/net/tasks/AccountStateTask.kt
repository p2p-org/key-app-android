package org.p2p.ethereumkit.spv.net.tasks

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.spv.core.ITask
import org.p2p.ethereumkit.spv.models.BlockHeader

class AccountStateTask(val address: EthAddress, val blockHeader: BlockHeader) : ITask
