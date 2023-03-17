package org.p2p.ethereumkit.internal.spv.net.devp2p.messages

import org.p2p.ethereumkit.internal.core.hexStringToByteArray
import org.p2p.ethereumkit.internal.spv.net.IInMessage

class PingMessage() : IInMessage {

    constructor(payload: ByteArray) : this()

    override fun toString(): String {
        return "Ping"
    }

    companion object {
        val payload = "C0".hexStringToByteArray()
    }
}
