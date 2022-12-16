package org.p2p.wallet.send.model

import android.os.Parcelable
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

@Parcelize
class SendFeeTotal constructor(
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: BigDecimal?,
    val sendFee: SendSolanaFee?,
    val feeLimit: FreeTransactionFeeLimit,
    val sourceSymbol: String,
    val recipientAddress: String
) : Parcelable {

    fun getTotalFee(resourceDelegate: (res: Int) -> String): String =
        when (sendFee) {
            is SendSolanaFee ->
                if (sourceSymbol == sendFee.feePayerSymbol) totalSum
                else "$totalFormatted + ${sendFee.accountCreationFormattedFee}"
            else ->
                resourceDelegate(R.string.send_fees_free)
        }

    fun getFees(resourceDelegate: (res: Int) -> String): String {
        if (sendFee == null) {
            return resourceDelegate(R.string.send_fees_free)
        }

        return sendFee.totalFee
    }

    fun getFeesFormatted(colorMountain: Int): CharSequence {
        if (sendFee == null) return emptyString()
        val usdText = sendFee.totalFeeUsdFormatted.orEmpty()
        return "${sendFee.totalFee} ${SpanUtils.highlightText(usdText, usdText, colorMountain)} "
    }

    val showAdditionalFee: Boolean
        get() = sendFee != null && sourceSymbol != sendFee.feePayerSymbol

    val showAccountCreation: Boolean
        // SendFee.SolanaFee is not null only if account creation is needed
        get() = sendFee != null

    val fullTotal: String
        get() = if (sourceSymbol == sendFee?.feePayerSymbol) {
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
        get() = "${(total + sendFee?.accountCreationFeeDecimals.orZero()).formatToken()} $sourceSymbol"
}
