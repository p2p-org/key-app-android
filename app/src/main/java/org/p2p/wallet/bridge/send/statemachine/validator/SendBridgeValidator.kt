package org.p2p.wallet.bridge.send.statemachine.validator

import java.math.BigDecimal
import org.p2p.core.utils.isMoreThan
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class SendBridgeValidator {

    fun validateInputAmount(token: SendToken.Bridge, newAmount: BigDecimal, newAmountWithFee: BigDecimal) {
        val maxAmount = token.tokenAmount
        if (newAmountWithFee.isMoreThan(maxAmount)) throw SendFeatureException.NotEnoughAmount(newAmount)
    }

    fun validateIsFeeMoreThanAmount(fee: SendFee.Bridge, newAmount: BigDecimal, newAmountWithFee: BigDecimal) {
        val feeSum = getFeeSum(fee)
        if (feeSum.isMoreThan(newAmountWithFee)) throw SendFeatureException.FeeIsMoreThanAmount(newAmount)
    }

    fun validateIsFeeMoreThanTotal(token: SendToken.Bridge, fee: SendFee.Bridge, newAmount: BigDecimal) {
        val totalAmount = token.tokenAmount
        val feeSum = getFeeSum(fee)
        if (feeSum > totalAmount) throw SendFeatureException.FeeIsMoreThanAmount(newAmount)
    }

    private fun getFeeSum(fee: SendFee.Bridge): BigDecimal {
        return fee.fee.getFeeList().sumOf { it.amountInToken }
    }
}
