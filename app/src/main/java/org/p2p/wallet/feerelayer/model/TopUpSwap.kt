package org.p2p.wallet.feerelayer.model

sealed class TopUpSwap {

    object Spl : TopUpSwap()
    object SplTransitive:TopUpSwap()
}