package org.p2p.ethereumkit.internal.spv.core

import org.p2p.ethereumkit.internal.spv.net.IInMessage
import org.p2p.ethereumkit.internal.spv.net.IOutMessage


interface IPeerListener {
    fun didConnect(peer: IPeer)
    fun didDisconnect(peer: IPeer, error: Throwable?)
}

interface IPeer : ITaskPerformer {
    val id: String
    var listener: IPeerListener?

    fun register(messageHandler: IMessageHandler)
    fun connect()
    fun disconnect(error: Throwable?)
}

interface ITaskPerformer {
    fun register(taskHandler: ITaskHandler)
    fun add(task: ITask)
}

interface ITask

interface ITaskHandler {
    fun perform(task: ITask, requester: ITaskHandlerRequester): Boolean
}

interface IMessageHandler {
    fun handle(peer: IPeer, message: IInMessage): Boolean
}

interface ITaskHandlerRequester {
    fun send(message: IOutMessage)
}
