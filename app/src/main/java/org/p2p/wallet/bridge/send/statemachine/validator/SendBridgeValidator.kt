package org.p2p.wallet.bridge.send.statemachine.validator

import java.math.BigDecimal
import org.p2p.core.utils.isMoreThan
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine.Companion.SEND_FEE_EXPIRED_DURATION
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class SendBridgeValidator {

    fun validateInputAmount(token: SendToken.Bridge, newAmount: BigDecimal) {
        val maxAmount = token.tokenAmount
        if (newAmount.isMoreThan(maxAmount)) throw SendFeatureException.NotEnoughAmount(newAmount)
    }

    fun validateIsFeeMoreThanAmount(state: SendState.Static, fee: SendFee.Bridge) {
        val inputAmount = state.inputAmount ?: return

        val feeSum = getFeeSum(fee)
        if (feeSum.isMoreThan(inputAmount)) throw SendFeatureException.InsufficientFunds(inputAmount)
    }

    fun validateIsFeeMoreThanTotal(state: SendState.Static, fee: SendFee.Bridge) {
        val totalAmount = state.bridgeToken?.tokenAmount
        val feeSum = getFeeSum(fee)
        if (totalAmount != null && feeSum > totalAmount) throw SendFeatureException.FeeIsMoreThanTotal(totalAmount)
    }

    fun isFeeValid(oldFee: SendFee.Bridge?): Boolean {
        val now = System.currentTimeMillis()
        return when {
            oldFee == null -> false
            (now - oldFee.updateTimeMs) < SEND_FEE_EXPIRED_DURATION -> true
            else -> false
        }
    }

    private fun getFeeSum(fee: SendFee.Bridge): BigDecimal {
        return fee.fee.arbiterFee.amountInToken
    }
}
