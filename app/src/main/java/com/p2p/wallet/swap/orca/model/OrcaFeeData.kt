package com.p2p.wallet.swap.orca.model

data class OrcaFeeData(
    val networkFee: String,
    val liquidityProviderFee: String,
    val paymentOption: String
)