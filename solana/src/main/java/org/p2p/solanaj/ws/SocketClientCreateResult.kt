package org.p2p.solanaj.ws

sealed interface SocketClientCreateResult {
    class Created(val instance: SubscriptionSocketClient) : SocketClientCreateResult
    class Failed(override val cause: Throwable) : Throwable(), SocketClientCreateResult
}
