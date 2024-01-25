package org.p2p.wallet.bridge.send.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class SendBridgeStaticStateMapper {

    fun updateFee(oldState: SendState.Static, newFee: SendFee.Bridge): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.ReadyToSend -> mapTokenNotZero(oldState.token, oldState.amount, newFee)
            is SendState.Static.TokenNotZero -> oldState.copy(fee = newFee)
            is SendState.Static.TokenZero -> oldState.copy(fee = newFee)
        }
    }

    fun updateInputAmount(oldState: SendState.Static, newAmount: BigDecimal): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.ReadyToSend -> mapTokenNotZero(oldState.token, newAmount, oldState.fee)
            is SendState.Static.TokenNotZero -> oldState.copy(amount = newAmount)
            is SendState.Static.TokenZero -> mapTokenNotZero(oldState.token, newAmount, oldState.fee)
        }
    }

    private fun mapTokenNotZero(
        token: SendToken,
        amount: BigDecimal,
        fee: SendFee?
    ): SendState.Static.TokenNotZero {
        return SendState.Static.TokenNotZero(
            token = token,
            amount = amount,
            fee = fee
        )
    }
}
