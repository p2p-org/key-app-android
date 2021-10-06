package com.p2p.wallet.swap.model

data class PriceData(
    val sourceAmount: String,
    val destinationAmount: String,
    val sourceSymbol: String,
    val destinationSymbol: String
)