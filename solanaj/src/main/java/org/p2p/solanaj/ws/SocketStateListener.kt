package org.p2p.solanaj.ws

import java.lang.Exception

interface SocketStateListener {
    fun onConnected()
    fun onWebSocketPong()
    fun onFailed(exception: Exception)
    fun onClosed(code: Int, message: String)
}
