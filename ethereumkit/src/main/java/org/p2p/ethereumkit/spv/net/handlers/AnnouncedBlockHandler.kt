package org.p2p.ethereumkit.spv.net.handlers

import org.p2p.ethereumkit.spv.core.IMessageHandler
import org.p2p.ethereumkit.spv.core.IPeer
import org.p2p.ethereumkit.spv.net.IInMessage
import org.p2p.ethereumkit.spv.net.les.messages.AnnounceMessage

class AnnouncedBlockHandler(private var listener: Listener? = null) : IMessageHandler {

    interface Listener {
        fun didAnnounce(peer: IPeer, blockHash: ByteArray, blockHeight: Long)
    }

    override fun handle(peer: IPeer, message: IInMessage): Boolean {
        if (message !is AnnounceMessage) {
            return false
        }

        listener?.didAnnounce(peer, message.blockHash, message.blockHeight)

        return true
    }
}
