package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toUsd
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeesStringFormat
import org.p2p.wallet.newsend.model.SendFeeTotal

class BridgeSendUiMapper(private val resources: Resources) {

    fun buildTotalFee(
        sourceToken: Token.Active,
        calculationMode: CalculationMode,
        recipient: EthAddress,
        feeLimitInfo: FreeTransactionFeeLimit,
    ): SendFeeTotal {
        val currentAmount = calculationMode.getCurrentAmount()
        return SendFeeTotal(
            currentAmount = currentAmount,
            currentAmountUsd = calculationMode.getCurrentAmountUsd(),
            receive = "${currentAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = currentAmount.toUsd(sourceToken),
            sourceSymbol = sourceToken.tokenSymbol,
            sendFee = null, // TODO fix this
            recipientAddress = recipient.hex,
            feeLimit = feeLimitInfo
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
            tokenAmount = this?.amountInToken(decimals).takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }
}
