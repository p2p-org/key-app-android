package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.R
import org.p2p.wallet.utils.formatToken
import java.math.BigDecimal

data class SwapTotal(
    val fee: SwapFee?,
    val destinationAmount: String,
    val inputAmount: BigDecimal,
    val inputAmountUsd: BigDecimal?,
    val receiveAtLeast: String,
    val receiveAtLeastUsd: String?
) {

    fun getFormattedTotal(split: Boolean): String {
        if (fee == null) {
            inputAmount.formatToken()
        }

        return ""
    }

    val fullTotal: String
        get() = if (approxTotalUsd != null) "$inputAmount $approxTotalUsd" else inputAmount.formatToken()

    val approxTotalUsd: String? get() = inputAmountUsd?.let { "(~$it)" }

//    val fullFee: String?
//        get() = fee?.commonFee

    val fullReceiveAtLeast: String
        get() = if (approxReceiveAtLeast != null) "$receiveAtLeast $approxReceiveAtLeast" else receiveAtLeast

    val approxReceiveAtLeast: String?
        get() = receiveAtLeastUsd?.let { "(~$it)" }

//    val totalAmount = if (inputAmount != null) {
//        if (inputAmount.fee == null) inputAmount.total.formatToken() else "${inputAmount.total} + ${inputAmount.fee.accountCreationFee}"
//    } else {
//        context.getString(R.string.swap_total_zero_sol)
//    }
}
