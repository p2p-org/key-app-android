package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import java.math.BigDecimal
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

class BridgeSendUiMapper(private val resources: Resources) {

    fun makeBridgeFeeDetails(
        recipientAddress: String,
        tokenToSend: Token.Active,
        calculationMode: CalculationMode,
        fees: BridgeSendFees?
    ): BridgeFeeDetails {
        val tokenSymbol = tokenToSend.tokenSymbol
        val decimals = tokenToSend.decimals
        val resultAmount = getResultAmount(tokenSymbol, decimals, calculationMode, fees)
        return BridgeFeeDetails(
            recipientAddress = recipientAddress,
            willGetAmount = resultAmount.toBridgeAmount(tokenSymbol, decimals),
            networkFee = fees?.networkFee.toBridgeAmount(tokenSymbol, decimals),
            messageAccountRent = fees?.messageAccountRent.toBridgeAmount(tokenSymbol, decimals),
            bridgeFee = fees?.arbiterFee.toBridgeAmount(tokenSymbol, decimals),
            total = resultAmount.toBridgeAmount(tokenSymbol, decimals)
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
            inputAmount - feesList.sumOf { it.amountInToken(decimals) }
        }
        val totalAmountUsd = if (calculationMode.isCurrentInputEmpty()) {
            BigDecimal.ZERO
        } else {
            inputAmountUsd - feesList.sumOf { it.amountInUsd.toBigDecimalOrZero() }
        }
        return BridgeFee(
            totalAmount.toPlainString(),
            totalAmountUsd.toPlainString(),
            chain = null,
            token = tokenSymbol
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

    private fun BridgeFee?.toBridgeAmount(
        tokenSymbol: String,
        decimals: Int,
    ): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = tokenSymbol,
            tokenDecimals = decimals,
            tokenAmount = this?.amountInToken(decimals).takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }
}
