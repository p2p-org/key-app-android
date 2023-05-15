package org.p2p.wallet.updates

enum class SocketState {
    DISCONNECTED,
    INITIALIZING,
    INITIALIZATION_FAILED,
    CONNECTING,
    CONNECTING_FAILED,
    CONNECTED
}
