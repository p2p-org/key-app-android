package org.p2p.wallet.updates

enum class UpdatesState {
    DISCONNECTED,
    INITIALIZING,
    INITIALIZATION_FAILED,
    CONNECTING,
    CONNECTING_FAILED,
    CONNECTED
}
