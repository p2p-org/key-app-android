package org.p2p.wallet.swap.model.orca

import org.p2p.core.token.Token
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import java.math.BigDecimal

class SwapTotal(
    val destinationAmount: String,
    private val fee: SwapFee?,
    private val sourceToken: Token.Active,
    private val destination: Token,
    private val inputAmount: BigDecimal,
    private val receiveAtLeastDecimals: BigDecimal
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

        val feeSolTotal = "${fee.feeAmountInSol} ${fee.feePayerSymbol}"

        /*
         * Source token is definitely SPL
         * Validating if user pays with SOL or SPL
         * */
        return if (sourceToken.tokenSymbol == fee.feePayerSymbol) {
            "${inputAmount + fee.feeAmountInPayingToken} ${fee.feePayerSymbol}"
        } else {
            if (split) "$inputTotal \n$feeSolTotal" else "$inputTotal + $feeSolTotal"
        }
    }

    val receiveAtLeast: String = "${receiveAtLeastDecimals.formatToken()} ${destination.tokenSymbol}"

    val receiveAtLeastUsd: String?
        get() {
            val receiveAtLeastUsd = destination.rate?.let { receiveAtLeastDecimals.multiply(it) }
            return receiveAtLeastUsd?.formatFiat()
        }

    val inputAmountUsd: BigDecimal?
        get() = sourceToken.rate?.let { inputAmount.multiply(it) }

    val approxTotalUsd: String? get() = inputAmountUsd?.let { "(~$it)" }

    val fullReceiveAtLeast: String
        get() = if (approxReceiveAtLeast != null) "$receiveAtLeast $approxReceiveAtLeast" else receiveAtLeast

    val approxReceiveAtLeast: String?
        get() = receiveAtLeastUsd?.let { "(~$it)" }
}
