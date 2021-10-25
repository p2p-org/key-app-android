package org.p2p.wallet.swap.model

data class PriceData(
    val sourceAmount: String,
    val destinationAmount: String,
    val sourceSymbol: String,
    val destinationSymbol: String
) {

    fun getPrice(isReverse: Boolean) = if (isReverse) {
        "$destinationAmount $destinationSymbol per $sourceSymbol"
    } else {
        "$sourceAmount $sourceSymbol per $destinationSymbol"
    }
}