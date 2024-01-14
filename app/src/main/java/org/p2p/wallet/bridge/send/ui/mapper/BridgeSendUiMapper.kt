package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import android.view.Gravity
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import org.p2p.core.model.TextHighlighting
import org.p2p.core.model.TitleValue
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.toBridgeAmount
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
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
            willGetAmount = fees?.recipientGetsAmount.toBridgeAmount(),
            networkFee = fees?.networkFee.toBridgeAmount(),
            messageAccountRent = fees?.messageAccountRent.toBridgeAmount(),
            bridgeFee = fees?.arbiterFee.toBridgeAmount(),
            totalAmount = fees?.totalAmount.toBridgeAmount()
        )
    }

    fun prepareShowProgress(
        iconUrl: String,
        amountTokens: String,
        amountUsd: String?,
        recipient: String,
        feeDetails: BridgeFeeDetails?
    ): NewShowProgress {
        val transactionDate = ZonedDateTime.now()
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = iconUrl,
            amountTokens = amountTokens,
            amountUsd = amountUsd,
            totalFees = feeDetails?.bridgeFee?.toTextHighlighting()?.let { listOf(it) },
            transactionDetails = listOf(TitleValue(resources.getString(R.string.transaction_send_to_title), recipient))
        )
    }

    fun getFeesFormattedUsd(bridgeFee: SendFee.Bridge?, isInputEmpty: Boolean): String {
        return getFeesInUsd(
            bridgeFee = bridgeFee,
            isInputEmpty = isInputEmpty
        )
    }

    fun getFeesFormattedToken(
        token: SendToken.Bridge,
        bridgeFee: SendFee.Bridge?,
        isInputEmpty: Boolean
    ): String {
        return getFeesInToken(
            token = token,
            bridgeFee = bridgeFee,
            isInputEmpty = isInputEmpty
        )
    }

    private fun getFeesInUsd(
        bridgeFee: SendFee.Bridge?,
        isInputEmpty: Boolean
    ): String {
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

    private fun getFeesInToken(
        token: SendToken.Bridge,
        bridgeFee: SendFee.Bridge?,
        isInputEmpty: Boolean
    ): String {
        if (bridgeFee == null) {
            return if (isInputEmpty) {
                resources.getString(R.string.send_fees_free)
            } else {
                "${BigDecimal.ZERO.formatToken(token.token.decimals)} ${token.token.tokenSymbol}"
            }
        }
        val fees = bridgeFee.fee
        val feeList = listOf(
            fees.networkFeeInToken,
            fees.messageAccountRentInToken,
            fees.bridgeFeeInToken,
            fees.arbiterFee
        )
        val fee: BigDecimal = feeList.sumOf { it.amountInToken }
        return "${fee.formatToken(token.token.decimals)} ${token.token.tokenSymbol}"
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

    private fun BridgeAmount.toTextHighlighting(): TextHighlighting? {
        if (isZero) return null
        val usdText = fiatAmount?.asApproximateUsd(withBraces = true).orEmpty()
        val commonText = "$formattedTokenAmount $usdText"
        return TextHighlighting(
            commonText = commonText,
            highlightedText = usdText
        )
    }
}
