package org.p2p.ethereumkit.spv.net.tasks

import org.p2p.ethereumkit.spv.core.ITask
import org.p2p.ethereumkit.models.RawTransaction
import org.p2p.ethereumkit.models.Signature

class SendTransactionTask(val sendId: Int,
                          val rawTransaction: RawTransaction,
                          val signature: Signature) : ITask
