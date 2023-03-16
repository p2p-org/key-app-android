package org.p2p.ethereumkit.internal.spv.net.connection

import org.p2p.core.wrapper.eth.ECKey
import org.p2p.ethereumkit.internal.spv.net.Node


class FrameConnection(private val connection: Connection) : Connection.Listener {

    interface Listener {
        fun didConnect()
        fun didDisconnect(error: Throwable?)
        fun didReceive(packetType: Int, payload: ByteArray)
    }

    var listener: Listener? = null

    fun connect() {
        connection.connect()
    }

    fun disconnect(error: Throwable?) {
        connection.disconnect(error)
    }

    fun send(packetType: Int, payload: ByteArray) {
        val frame = Frame(packetType, payload)
        connection.send(frame)
    }

    // ----- Connection.Listener methods ----

    override fun didConnect() {
        listener?.didConnect()
    }

    override fun didDisconnect(error: Throwable?) {
        listener?.didDisconnect(error)
    }

    override fun didReceive(frame: Frame) {
        listener?.didReceive(frame.type, frame.payload)
    }

    companion object {

        fun getInstance(connectionKey: ECKey, node: Node): FrameConnection {
            val connection = Connection(connectionKey, node)
            val frameConnection = FrameConnection(connection)

            connection.listener = frameConnection

            return frameConnection
        }
    }
}
