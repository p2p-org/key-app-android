package org.p2p.ethereumkit.internal.spv.net.devp2p.messages

import org.p2p.ethereumkit.internal.core.hexStringToByteArray
import org.p2p.ethereumkit.internal.spv.net.IInMessage
import org.p2p.ethereumkit.internal.spv.net.IOutMessage

class PongMessage() : IInMessage, IOutMessage {

    constructor(payload: ByteArray) : this()

    override fun encoded(): ByteArray {
        return payload
    }

    override fun toString(): String {
        return "Pong"
    }

    companion object {
        val payload = "C0".hexStringToByteArray()
    }
}
