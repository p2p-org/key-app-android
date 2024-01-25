package org.p2p.wallet.send.model

import androidx.annotation.ColorInt
import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.model.TextHighlighting
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

/**
 * [SendSolanaFee] can be null only if total fees is Zero. (transaction fee and account creation fee)
 * */

/**
 * @param transferFeePercent percentage (e.g. 6.65)
 * @param interestBearingPercent percentage (e.g. 6.65)
 */
@Parcelize
class SendFeeTotal constructor(
    val currentAmount: BigDecimal,
    val currentAmountUsd: BigDecimal?,
    val receiveFormatted: String,
    val receiveUsd: BigDecimal?,
    val sendFee: SendSolanaFee?,
    val feeLimit: TransactionFeeLimits,
    val sourceSymbol: String,
    val recipientAddress: String,
    val transferFeePercent: BigDecimal? = null,
    val interestBearingPercent: BigDecimal? = null
) : Parcelable {

    @IgnoredOnParcel
    val isSendingToken2022: Boolean
        get() = transferFeePercent != null || interestBearingPercent != null

    fun getFeesInToken(isInputEmpty: Boolean): FeesStringFormat {
        return when {
            isSendingToken2022 -> {
                FeesStringFormat(R.string.send_fees_token2022_format)
            }
            sendFee == null -> {
                val textRes = if (isInputEmpty) R.string.send_fees_free else R.string.send_fees_zero
                FeesStringFormat(textRes)
            }
            else -> {
                FeesStringFormat(R.string.send_fees_format, sendFee.totalFee)
            }
        }
    }

    fun formatTotalCombined(@ColorInt colorMountain: Int): CharSequence {
        if (sendFee == null || sendFee.feePayerSymbol != sourceSymbol) {
            val usdText = currentAmountUsd?.asApproximateUsd().orEmpty()
            val totalText = "$totalSumWithSymbol $usdText"
            return SpanUtils.highlightText(totalText, usdText, colorMountain)
        }

        // if fee and source token is the same, we'll have only one field for fees
        val totalAmount = currentAmount + sendFee.totalFeeDecimals
        val totalAmountUsd = currentAmountUsd.orZero()
            .plus(sendFee.totalFeeDecimalsUsd.orZero())
            .asApproximateUsd()
        val totalText = "$totalSumWithSymbol $totalAmountUsd"
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
            if (approxTotalUsd != null) "$totalSumWithSymbol $approxTotalUsd" else totalSumWithSymbol
        } else {
            if (approxTotalUsd != null) "$totalWithSymbolFormatted $approxTotalUsd" else totalWithSymbolFormatted
        }

    val approxTotalUsd: String? get() = currentAmountUsd?.asApproximateUsd()

    val fullReceive: String
        get() = if (approxReceiveUsd.isNotBlank()) "$receiveFormatted $approxReceiveUsd" else receiveFormatted

    val approxReceiveUsd: String
        get() = receiveUsd?.asApproximateUsd().orEmpty()

    private val totalWithSymbolFormatted: String
        get() = currentAmount.formatTokenWithSymbol(sourceSymbol)

    private val totalSumWithSymbol: String
        get() {
            val transferFee = transferFeePercent
                ?.let { it / 100.toBigDecimal() }
                ?.multiply(currentAmount)

            val totalSum = currentAmount
                .run {
                    // is fee is paid by other token
                    if (sendFee?.feePayerSymbol == sourceSymbol) {
                        plus(sendFee.totalFeeDecimals.orZero())
                    } else {
                        this
                    }
                }
                .plus(transferFee.orZero())
            return totalSum.formatTokenWithSymbol(sourceSymbol)
        }
}
