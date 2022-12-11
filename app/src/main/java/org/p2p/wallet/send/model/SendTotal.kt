package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import java.math.BigDecimal

@Parcelize
class SendTotal constructor(
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: BigDecimal?,
    val fee: SendFee?,
    val sourceSymbol: String,
    var recipientAddress: String? = null
) : Parcelable {

    fun getTotalFee(): String =
        when (fee) {
            is SendFee.SolanaFee ->
                if (sourceSymbol == fee.feePayerSymbol) totalSum
                else "$totalFormatted + ${fee.feeDecimals} ${fee.feePayerSymbol}"
            is SendFee.RenBtcFee ->
                "$totalFormatted + ${fee.feeDecimals} ${fee.feePayerSymbol}"
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
        get() = "${total.formatToken()} $sourceSymbol"

    private val totalSum: String
        get() = "${(total + fee?.feeDecimals.orZero()).formatToken()} $sourceSymbol"
}
