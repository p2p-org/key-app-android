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
            willGetAmount = resultAmount.toBridgeAmount(),
            networkFee = fees?.networkFee.toBridgeAmount(),
            messageAccountRent = fees?.messageAccountRent.toBridgeAmount(),
            bridgeFee = fees?.arbiterFee.toBridgeAmount(),
            total = resultAmount.toBridgeAmount()
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
            chain = null,
            token = tokenSymbol,
            symbol = tokenSymbol,
            decimals = decimals,
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
}
