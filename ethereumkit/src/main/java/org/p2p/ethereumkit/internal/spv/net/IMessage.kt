package org.p2p.ethereumkit.internal.spv.net

interface IMessage

interface IInMessage : IMessage

interface IOutMessage : IMessage {
    fun encoded(): ByteArray
}
