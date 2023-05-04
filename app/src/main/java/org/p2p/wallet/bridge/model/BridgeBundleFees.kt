package org.p2p.wallet.bridge.model

data class BridgeBundleFees(
    val gasFee: BridgeFee,
    val gasFeeInToken: BridgeFee,
    val arbiterFee: BridgeFee,
    val createAccount: BridgeFee,
)
