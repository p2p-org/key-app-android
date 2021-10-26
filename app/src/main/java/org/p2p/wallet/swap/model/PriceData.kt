package org.p2p.wallet.swap.model

data class PriceData(
    val sourcePrice: String,
    val destinationPrice: String,
    val sourceSymbol: String,
    val destinationSymbol: String
) {

    fun getPrice(isReverse: Boolean) = if (isReverse) {
        "$destinationPrice $destinationSymbol per $sourceSymbol"
    } else {
        "$sourcePrice $sourceSymbol per $destinationSymbol"
    }
}