package org.p2p.ethereumkit.internal.spv.core

import org.p2p.ethereumkit.internal.core.TransactionBuilder
import org.p2p.ethereumkit.internal.models.Transaction
import org.p2p.ethereumkit.internal.models.RawTransaction
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.ethereumkit.internal.spv.net.handlers.SendTransactionTaskHandler
import org.p2p.ethereumkit.internal.spv.net.tasks.SendTransactionTask

class TransactionSender(
        private val transactionBuilder: TransactionBuilder,
) : SendTransactionTaskHandler.Listener {

    interface Listener {
        fun onSendSuccess(sendId: Int, transaction: Transaction)
        fun onSendFailure(sendId: Int, error: Throwable)
    }

    var listener: Listener? = null

    fun send(sendId: Int, taskPerformer: ITaskPerformer, rawTransaction: RawTransaction, signature: Signature) {
        taskPerformer.add(SendTransactionTask(sendId, rawTransaction, signature))
    }

    override fun onSendSuccess(task: SendTransactionTask) {
        val transaction = transactionBuilder.transaction(task.rawTransaction, task.signature)

        listener?.onSendSuccess(task.sendId, transaction)
    }

    override fun onSendFailure(task: SendTransactionTask, error: Throwable) {
        listener?.onSendFailure(task.sendId, error)
    }

}
