package org.p2p.wallet.send.model

import org.p2p.wallet.utils.AmountUtils
import org.p2p.wallet.utils.asApproximateUsd
import java.math.BigDecimal

class SendTotal constructor(
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: BigDecimal?,
    val fee: SendFee?,
    val sourceSymbol: String
) {

    fun getTotalFee(): String =
        when (fee) {
            is SendFee.SolanaFee ->
                if (sourceSymbol == fee.feePayerSymbol) totalSum
                else "$totalFormatted + ${fee.fee} ${fee.feePayerSymbol}"
            is SendFee.RenBtcFee ->
                "$totalFormatted + ${fee.fee} ${fee.feePayerSymbol}"
            else ->
                totalFormatted
        }

    val showAdditionalFee: Boolean
        get() = fee != null && sourceSymbol != fee.feePayerSymbol

    val showAccountCreation: Boolean
        // SendFee.SolanaFee is not null only if account creation is needed
        get() = fee != null && fee is SendFee.SolanaFee

    val fullTotal: String
        get() = if (sourceSymbol == fee?.feePayerSymbol) {
            if (approxTotalUsd != null) "$totalSum $approxTotalUsd" else totalSum
        } else {
            if (approxTotalUsd != null) "$totalFormatted $approxTotalUsd" else totalFormatted
        }

    val approxTotalUsd: String? get() = totalUsd?.asApproximateUsd()

    val fullReceive: String
        get() = if (approxReceive.isNotBlank()) "$receive $approxReceive" else receive

    val approxReceive: String
        get() = receiveUsd?.asApproximateUsd().orEmpty()

    private val totalFormatted: String
        get() = "${AmountUtils.format(total)} $sourceSymbol"

    private val totalSum: String
        get() = "${AmountUtils.format(total + (fee?.fee ?: BigDecimal.ZERO))} $sourceSymbol"
}
