package org.p2p.solanaj.ws

interface SocketStateListener {
    fun onConnected()
    fun onWebSocketPong()
    fun onFailed(exception: Exception)
    fun onClientClosed(code: Int, message: String)
}
