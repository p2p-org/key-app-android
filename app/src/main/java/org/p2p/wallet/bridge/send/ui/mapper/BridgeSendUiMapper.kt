package org.p2p.wallet.bridge.send.ui.mapper

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.newsend.model.FeesStringFormat

class BridgeSendUiMapper(private val resources: Resources) {

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
