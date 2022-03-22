package org.p2p.wallet.send.model

sealed class SwapStrategy {
    object SimpleRelay : SwapStrategy()
    object TopUp : SwapStrategy()
}
