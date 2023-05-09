package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import android.view.Gravity
import java.math.BigDecimal
import java.util.Date
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.ui.model.BridgeFeeDetails
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.toPx

class BridgeSendUiMapper(private val resources: Resources) {

    fun makeBridgeFeeDetails(
        recipientAddress: String,
        fees: BridgeSendFees?
    ): BridgeFeeDetails {
        return BridgeFeeDetails(
            recipientAddress = recipientAddress,
            willGetAmount = fees?.resultAmount.toBridgeAmount(),
            networkFee = fees?.networkFee.toBridgeAmount(),
            messageAccountRent = fees?.messageAccountRent.toBridgeAmount(),
            bridgeFee = fees?.arbiterFee.toBridgeAmount()
        )
    }

    fun prepareShowProgress(
        tokenToSend: Token.Active,
        amountTokens: String,
        amountUsd: String?,
        recipient: String,
        feeDetails: BridgeFeeDetails?
    ): NewShowProgress {
        val transactionDate = Date()
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = tokenToSend.iconUrl.orEmpty(),
            amountTokens = amountTokens,
            amountUsd = amountUsd,
            recipient = recipient,
            totalFees = feeDetails?.bridgeFee?.toTextHighlighting()?.let { listOf(it) }
        )
    }

    fun getFeesFormatted(bridgeFee: SendFee.Bridge?, isInputEmpty: Boolean): String {
        return getFeesInToken(
            bridgeFee = bridgeFee,
            isInputEmpty = isInputEmpty
        )
    }

    private fun getFeesInToken(bridgeFee: SendFee.Bridge?, isInputEmpty: Boolean): String {
        if (bridgeFee == null) {
            return if (isInputEmpty) {
                resources.getString(R.string.send_fees_free)
            } else {
                BigDecimal.ZERO.asApproximateUsd()
            }
        }
        val fees = bridgeFee.fee
        val feeList = listOf(
            fees.networkFeeInToken,
            fees.messageAccountRentInToken,
            fees.bridgeFeeInToken,
            fees.arbiterFee
        )
        val fee: BigDecimal = feeList.sumOf { it.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        return fee.asApproximateUsd(withBraces = false)
    }

    fun getFeeTextSkeleton(): TextViewCellModel.Skeleton {
        return TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 16.toPx(),
                width = 56.toPx(),
                radius = 4f.toPx(),
                gravity = Gravity.END
            )
        )
    }

    private fun BridgeFee?.toBridgeAmount(): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = this?.symbol.orEmpty(),
            tokenDecimals = this?.decimals.orZero(),
            tokenAmount = this?.amountInToken?.takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }

    private fun BridgeAmount.toTextHighlighting(): TextHighlighting? {
        if (isZero) return null
        val usdText = formattedFiatAmount.orEmpty()
        val commonText = "$formattedTokenAmount $usdText"
        return TextHighlighting(
            commonText = commonText,
            highlightedText = usdText
        )
    }
}
