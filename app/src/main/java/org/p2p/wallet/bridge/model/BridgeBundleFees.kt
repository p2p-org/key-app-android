package org.p2p.wallet.bridge.model

data class BridgeBundleFees(
    val gasEth: BridgeFee,
    val arbiterFee: BridgeFee,
    val createAccount: BridgeFee,
)
