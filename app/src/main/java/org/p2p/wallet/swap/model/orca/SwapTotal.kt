package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.formatToken
import java.math.BigDecimal

data class SwapTotal(
    val fee: SwapFee?,
    val feePayerToken: Token.Active,
    val sourceToken: Token.Active,
    val destination: Token,
    val destinationAmount: String,
    val inputAmount: BigDecimal,
    val inputAmountUsd: BigDecimal?,
    val receiveAtLeast: String,
    val receiveAtLeastUsd: String?
) {

    fun getFormattedTotal(split: Boolean): String {
        val inputTotal = "${inputAmount.formatToken()} ${sourceToken.tokenSymbol}"

        /*
        * Showing only input for the total field
        * */
        if (fee == null) {
            return inputTotal
        }

        /*
         * If source is SOL then fee payer can be only SOL as well
         * */
        if (sourceToken.isSOL) {
            return "${inputAmount + fee.feeAmountInSol} ${sourceToken.tokenSymbol}"
        }

        val feeSolTotal = "${fee.feeAmountInSol} ${feePayerToken.tokenSymbol}"

        /*
         * Source token is definitely SPL
         * Validating if user pays with SOL or SPL
         * */
        return if (sourceToken.tokenSymbol == feePayerToken.tokenSymbol) {
            "${inputAmount + fee.feeAmountInPayingToken} ${feePayerToken.tokenSymbol}"
        } else {
            if (split) "$inputTotal \n$feeSolTotal" else "$inputTotal + $feeSolTotal"
        }
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
