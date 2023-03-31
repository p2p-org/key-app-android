package org.p2p.wallet.bridge.model

data class BridgeBundleFees(
    val gasEth: BridgeBundleFee,
    val arbiterFee: BridgeBundleFee,
    val createAccount: BridgeBundleFee,
)
