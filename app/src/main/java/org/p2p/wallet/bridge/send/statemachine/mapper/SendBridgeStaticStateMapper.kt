package org.p2p.wallet.bridge.send.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class SendBridgeStaticStateMapper {

    fun updateFee(oldState: SendState.Static, newFee: SendFee.Bridge): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.ReadyToSend -> oldState.copy(fee = newFee)
            is SendState.Static.TokenNotZero -> oldState.copy(fee = newFee)
            is SendState.Static.TokenZero -> oldState.copy(fee = newFee)
            is SendState.Static.Initialize -> oldState
        }
    }

    fun updateInputAmount(oldState: SendState.Static, newAmount: BigDecimal): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.Initialize -> oldState
            is SendState.Static.ReadyToSend -> oldState.copy(amount = newAmount)
            is SendState.Static.TokenNotZero ->
                if (oldState.fee == null) {
                    oldState.copy(amount = newAmount)
                } else {
                    mapReadyToSend(oldState.token, newAmount, oldState.fee)
                }

            is SendState.Static.TokenZero ->
                if (oldState.fee == null) {
                    mapTokenNotZero(oldState.token, newAmount, null)
                } else {
                    mapReadyToSend(oldState.token, newAmount, oldState.fee)
                }
        }
    }

    private fun mapReadyToSend(token: SendToken, amount: BigDecimal, fee: SendFee): SendState.Static.ReadyToSend {
        return SendState.Static.ReadyToSend(
            token = token,
            amount = amount,
            fee = fee
        )
    }

    private fun mapTokenNotZero(token: SendToken, amount: BigDecimal, fee: SendFee?): SendState.Static.TokenNotZero {
        return SendState.Static.TokenNotZero(
            token = token,
            amount = amount,
            fee = fee
        )
    }
}
