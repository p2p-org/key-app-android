package org.p2p.ethereumkit.spv.net.handlers

import org.p2p.ethereumkit.spv.core.*
import org.p2p.ethereumkit.spv.helpers.RandomHelper
import org.p2p.ethereumkit.spv.net.IInMessage
import org.p2p.ethereumkit.spv.net.les.messages.SendTransactionMessage
import org.p2p.ethereumkit.spv.net.les.messages.TransactionStatus
import org.p2p.ethereumkit.spv.net.les.messages.TransactionStatusMessage
import org.p2p.ethereumkit.spv.net.tasks.SendTransactionTask

class SendTransactionTaskHandler(val listener: Listener? = null) : ITaskHandler, IMessageHandler {

    interface Listener {
        fun onSendSuccess(task: SendTransactionTask)
        fun onSendFailure(task: SendTransactionTask, error: Throwable)
    }

    private val tasks: MutableMap<Long, SendTransactionTask> = HashMap()

    override fun perform(task: ITask, requester: ITaskHandlerRequester): Boolean {
        if (task !is SendTransactionTask) {
            return false
        }

        val requestId = RandomHelper.randomLong()

        tasks[requestId] = task

        val message = SendTransactionMessage(requestId, task.rawTransaction, task.signature)

        requester.send(message)

        return true
    }

    override fun handle(peer: IPeer, message: IInMessage): Boolean {
        if (message !is TransactionStatusMessage) {
            return false
        }

        val task = tasks[message.requestID] ?: return false

        when (val status = message.statuses.firstOrNull()) {
            null -> listener?.onSendFailure(task, SendError.NoStatus())
            is TransactionStatus.Unknown -> listener?.onSendFailure(task, SendError.UnknownError())
            is TransactionStatus.Error -> listener?.onSendFailure(task, SendError.Error(status.message))
            else -> listener?.onSendSuccess(task)
        }
        return true
    }

    open class SendError : Exception() {
        class NoStatus : SendError()
        class UnknownError : SendError()
        class Error(override val message: String) : SendError()
    }
}
