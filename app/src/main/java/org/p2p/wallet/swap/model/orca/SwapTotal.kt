package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.utils.formatToken
import java.math.BigDecimal

data class SwapTotal(
    val fee: SwapFee?,
    val destinationAmount: String,
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receiveAtLeast: String,
    val receiveAtLeastUsd: String?
) {

    val fullTotal: String
        get() = if (approxTotalUsd != null) "$total $approxTotalUsd" else total.formatToken()

    val approxTotalUsd: String? get() = totalUsd?.let { "(~$it)" }

    val fullFee: String?
        get() = fee?.commonFee

    val fullReceiveAtLeast: String
        get() = if (approxReceiveAtLeast != null) "$receiveAtLeast $approxReceiveAtLeast" else receiveAtLeast

    val approxReceiveAtLeast: String?
        get() = receiveAtLeastUsd?.let { "(~$it)" }
}
