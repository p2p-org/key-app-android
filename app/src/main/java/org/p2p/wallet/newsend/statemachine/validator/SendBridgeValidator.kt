package org.p2p.wallet.newsend.statemachine.validator

import java.math.BigDecimal
import org.p2p.wallet.newsend.statemachine.SendStateMachine.Companion.SEND_FEE_EXPIRED_DURATION
import org.p2p.wallet.newsend.statemachine.model.SendFee
import org.p2p.wallet.newsend.statemachine.model.SendToken

class SendBridgeValidator {

    fun validateInputAmount(token: SendToken.Bridge, newAmount: BigDecimal) {
        val maxAmount = token.tokenAmount
        // todo
//        throw SendFeatureException.NotEnoughAmount(newAmount)
    }

    fun isFeeValid(oldFee: SendFee.Bridge?): Boolean {
        val now = System.currentTimeMillis()
        return when {
            oldFee == null -> false
            (now - oldFee.updateTimeMs) < SEND_FEE_EXPIRED_DURATION -> true
            else -> false
        }
    }
}
