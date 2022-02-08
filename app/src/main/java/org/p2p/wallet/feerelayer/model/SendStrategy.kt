package org.p2p.wallet.feerelayer.model

sealed class SendStrategy {
    object SimpleSol : SendStrategy()
    object Spl : SendStrategy()
    object FeeRelayer : SendStrategy()
}