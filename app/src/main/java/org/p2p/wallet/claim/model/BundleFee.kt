package org.p2p.wallet.claim.model

data class BundleFee(
    val gasEth: String,
    val gasUsdAmount: String,
    val arbiterFee: String,
    val arbiterFeeUsd: String
)
