package org.p2p.ethereumkit.internal.spv.net.tasks

import org.p2p.ethereumkit.internal.spv.core.ITask
import org.p2p.ethereumkit.internal.models.RawTransaction
import org.p2p.ethereumkit.internal.models.Signature

class SendTransactionTask(val sendId: Int,
                          val rawTransaction: RawTransaction,
                          val signature: Signature) : ITask
