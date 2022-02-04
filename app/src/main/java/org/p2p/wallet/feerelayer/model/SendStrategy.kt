package org.p2p.wallet.feerelayer.model

sealed class SendStrategy {
    object SimpleSol : SendStrategy()
    object SimpleSpl : SendStrategy()
    object FeeRelay : SendStrategy()
}