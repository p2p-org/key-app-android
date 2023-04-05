package org.p2p.wallet.newsend.model

import androidx.annotation.ColorInt
import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.p2p.core.model.TextHighlighting
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

/**
 * [SendSolanaFee] can be null only if total fees is Zero. (transaction fee and account creation fee)
 * */

@Parcelize
class SendFeeTotal constructor(
    val currentAmount: BigDecimal,
    val currentAmountUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: BigDecimal?,
    val sendFee: SendSolanaFee?,
    val feeLimit: TransactionFeeLimits,
    val sourceSymbol: String,
    val recipientAddress: String
) : Parcelable {

    @Deprecated("for old send screen, will be removed")
    fun getTotalFee(resourceDelegate: (res: Int) -> String): String =
        when (sendFee) {
            is SendSolanaFee ->
                if (sourceSymbol == sendFee.feePayerSymbol) totalSum
                else "$totalFormatted + ${sendFee.accountCreationFormattedFee}"
            else ->
                resourceDelegate(R.string.send_fees_free)
        }

    fun getFeesInToken(isInputEmpty: Boolean): FeesStringFormat {
        if (sendFee == null) {
            val textRes = if (isInputEmpty) R.string.send_fees_free else R.string.send_fees_zero
            return FeesStringFormat(textRes)
        }

        return FeesStringFormat(R.string.send_fees_format, sendFee.totalFee)
    }

    fun getTotalCombined(@ColorInt colorMountain: Int): CharSequence {
        if (sendFee == null || sendFee.feePayerSymbol != sourceSymbol) {
            val usdText = currentAmountUsd?.asApproximateUsd().orEmpty()
            val totalText = "${currentAmount.toPlainString()} $sourceSymbol $usdText"
            return SpanUtils.highlightText(totalText, usdText, colorMountain)
        }

        // if fee and source token is the same, we'll have only one field for fees
        val totalAmount = currentAmount + sendFee.totalFeeDecimals
        val totalAmountUsd = (currentAmountUsd.orZero() + sendFee.totalFeeDecimalsUsd.orZero()).asApproximateUsd()
        val totalText = "${totalAmount.toPlainString()} $sourceSymbol $totalAmountUsd"
        return SpanUtils.highlightText(totalText, totalAmountUsd, colorMountain)
    }

    fun getFeesCombined(@ColorInt colorMountain: Int, checkFeePayer: Boolean = true): CharSequence? {
        if (sendFee == null || (checkFeePayer && sourceSymbol == sendFee.feePayerSymbol)) return null

        val usdText = sendFee.summedFeeDecimalsUsd.orEmpty()
        val combinedFees = "${sendFee.totalFee} $usdText"
        return SpanUtils.highlightText(combinedFees, usdText, colorMountain)
    }

    fun getFeesCombined(checkFeePayer: Boolean = true): TextHighlighting? {
        if (sendFee == null || (checkFeePayer && sourceSymbol == sendFee.feePayerSymbol)) return null

        val usdText = sendFee.summedFeeDecimalsUsd.orEmpty()
        val combinedFees = "${sendFee.totalFee} $usdText"
        return TextHighlighting(
            commonText = combinedFees,
            highlightedText = usdText
        )
    }

    val showAdditionalFee: Boolean
        get() = sendFee != null && sourceSymbol != sendFee.feePayerSymbol

    val showAccountCreation: Boolean
        // SendFee.SolanaFee is not null only if account creation is needed
        get() = sendFee != null && !sendFee.isAccountCreationFree

    val fullTotal: String
        get() = if (sourceSymbol == sendFee?.feePayerSymbol) {
            if (approxTotalUsd != null) "$totalSum $approxTotalUsd" else totalSum
        } else {
            if (approxTotalUsd != null) "$totalFormatted $approxTotalUsd" else totalFormatted
        }

    val approxTotalUsd: String? get() = currentAmountUsd?.asApproximateUsd()

    val fullReceive: String
        get() = if (approxReceive.isNotBlank()) "$receive $approxReceive" else receive

    val approxReceive: String
        get() = receiveUsd?.asApproximateUsd().orEmpty()

    private val totalFormatted: String
        get() = "${currentAmount.formatToken()} $sourceSymbol"

    private val totalSum: String
        get() = "${(currentAmount + sendFee?.accountCreationFeeDecimals.orZero()).formatToken()} $sourceSymbol"
}
