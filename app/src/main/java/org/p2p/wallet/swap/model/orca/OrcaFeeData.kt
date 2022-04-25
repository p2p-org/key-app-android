package org.p2p.wallet.swap.model.orca

data class OrcaFeeData(
    val networkFee: String,
    val liquidityProviderFee: String,
    val paymentOption: String
)
