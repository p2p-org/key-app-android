package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import java.math.BigDecimal

@Parcelize
class SendFeeTotal constructor(
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: BigDecimal?,
    val fee: SendSolanaFee?,
    val feeLimit: FreeTransactionFeeLimit,
    val sourceSymbol: String,
    val recipientAddress: String
) : Parcelable {

    fun getTotalFee(resourceDelegate: (res: Int) -> String): String =
        when (fee) {
            is SendSolanaFee.SolanaFee ->
                if (sourceSymbol == fee.feePayerSymbol) totalSum
                else "$totalFormatted + ${fee.feeDecimals} ${fee.feePayerSymbol}"
            else ->
                resourceDelegate(R.string.send_fees_free)
        }

    val showAdditionalFee: Boolean
        get() = fee != null && sourceSymbol != fee.feePayerSymbol

    val showAccountCreation: Boolean
        // SendFee.SolanaFee is not null only if account creation is needed
        get() = fee != null && fee is SendSolanaFee.SolanaFee

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
        get() = "${total.formatToken()} $sourceSymbol"

    private val totalSum: String
        get() = "${(total + fee?.feeDecimals.orZero()).formatToken()} $sourceSymbol"
}
