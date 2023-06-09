package org.p2p.wallet.bridge.send.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.BridgeSendState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class SendBridgeStaticStateMapper {

    fun updateFee(oldState: BridgeSendState.Static, newFee: SendFee.Bridge): BridgeSendState.Static {
        return when (oldState) {
            BridgeSendState.Static.Empty -> oldState
            is BridgeSendState.Static.ReadyToSend -> mapTokenNotZero(oldState.token, oldState.amount, newFee)
            is BridgeSendState.Static.TokenNotZero -> oldState.copy(fee = newFee)
            is BridgeSendState.Static.TokenZero -> oldState.copy(fee = newFee)
        }
    }

    fun updateInputAmount(oldState: BridgeSendState.Static, newAmount: BigDecimal): BridgeSendState.Static {
        return when (oldState) {
            BridgeSendState.Static.Empty -> oldState
            is BridgeSendState.Static.ReadyToSend -> mapTokenNotZero(oldState.token, newAmount, oldState.fee)
            is BridgeSendState.Static.TokenNotZero -> oldState.copy(amount = newAmount)
            is BridgeSendState.Static.TokenZero -> mapTokenNotZero(oldState.token, newAmount, oldState.fee)
        }
    }

    private fun mapTokenNotZero(
        token: SendToken,
        amount: BigDecimal,
        fee: SendFee?
    ): BridgeSendState.Static.TokenNotZero {
        return BridgeSendState.Static.TokenNotZero(
            token = token,
            amount = amount,
            fee = fee
        )
    }
}
