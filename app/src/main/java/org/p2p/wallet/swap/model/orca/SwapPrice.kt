package org.p2p.wallet.swap.model.orca

data class SwapPrice(
    val sourceSymbol: String,
    val destinationSymbol: String,
    val sourcePrice: String,
    val destinationPrice: String,
    val sourcePriceInUsd: String?,
    val destinationPriceInUsd: String?
) {

    val fullSourcePrice: String
        get() = if (approxSourceUsd != null) "$sourcePrice $approxSourceUsd" else sourcePrice

    val approxSourceUsd: String? get() = sourcePriceInUsd?.let { "(~$it)" }

    val fullDestinationPrice: String
        get() = if (approxDestinationUsd != null) "$destinationPrice $approxDestinationUsd" else destinationPrice

    val approxDestinationUsd: String? get() = destinationPriceInUsd?.let { "(~$it)" }
}
