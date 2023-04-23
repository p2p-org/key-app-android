package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import java.math.BigDecimal
import java.util.Date
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.ui.model.BridgeFeeDetails
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeesStringFormat
import org.p2p.wallet.transaction.model.NewShowProgress

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
        val feeList = listOfNotNull(
            feeDetails?.networkFee,
            feeDetails?.messageAccountRent,
            feeDetails?.bridgeFee
        )
        return NewShowProgress(
            date = transactionDate,
            tokenUrl = tokenToSend.iconUrl.orEmpty(),
            amountTokens = amountTokens,
            amountUsd = amountUsd,
            recipient = recipient,
            totalFees = feeList.mapNotNull { it.toTextHighlighting() }
        )
    }

    private fun getResultAmount(
        tokenSymbol: String,
        decimals: Int,
        calculationMode: CalculationMode,
        fees: BridgeSendFees?
    ): BridgeFee {
        val feesList = listOfNotNull(
            fees?.arbiterFee,
            fees?.networkFee,
            fees?.bridgeFee,
            fees?.messageAccountRent
        )
        val inputAmount = calculationMode.getCurrentAmountLamports().toBigDecimal()
        val inputAmountUsd = calculationMode.getCurrentAmountUsd().orZero()
        val totalAmount = if (calculationMode.isCurrentInputEmpty()) {
            BigDecimal.ZERO
        } else {
            inputAmount - feesList.sumOf { it.amountInToken }
        }
        val totalAmountUsd = if (calculationMode.isCurrentInputEmpty()) {
            BigDecimal.ZERO
        } else {
            inputAmountUsd - feesList.sumOf { it.amountInUsd.toBigDecimalOrZero() }
        }
        return BridgeFee(
            amount = totalAmount.toPlainString(),
            amountInUsd = totalAmountUsd.toPlainString(),
            symbol = tokenSymbol,
            name = tokenSymbol,
            decimals = decimals,
            chain = null,
            token = null,
        )
    }

    fun getFeesFormatted(bridgeFee: SendFee.Bridge?, isInputEmpty: Boolean): String {
        val feesLabel = getFeesInToken(
            bridgeFee = bridgeFee,
            isInputEmpty = isInputEmpty
        )
        return feesLabel.format(resources)
    }

    private fun getFeesInToken(bridgeFee: SendFee.Bridge?, isInputEmpty: Boolean): FeesStringFormat {
        if (bridgeFee == null) {
            val textRes = if (isInputEmpty) R.string.send_fees_free else R.string.send_fees_zero
            return FeesStringFormat(textRes)
        }
        val fees = bridgeFee.fee
        val feeList = listOf(fees.networkFee, fees.messageAccountRent, fees.bridgeFee, fees.arbiterFee)
        val fee: BigDecimal = feeList.sumOf { it.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        val feesLabel = fee.asApproximateUsd(withBraces = false)

        return FeesStringFormat(R.string.send_fees_format, feesLabel)
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
