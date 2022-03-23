package org.p2p.wallet.swap.model

data class PriceData(
    val inputPrice: String,
    val outputPrice: String,
    val inputSymbol: String,
    val outputSymbol: String
)
