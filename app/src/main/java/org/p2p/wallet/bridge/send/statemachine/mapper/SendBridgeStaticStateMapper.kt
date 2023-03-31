package org.p2p.wallet.bridge.send.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee

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
            is SendState.Static.ReadyToSend -> SendState.Static.TokenNotZero(
                token = oldState.token,
                amount = newAmount,
                fee = null
            )
            is SendState.Static.TokenNotZero -> oldState.copy(amount = newAmount)
            is SendState.Static.TokenZero ->
                SendState.Static.TokenNotZero(
                    token = oldState.token,
                    amount = newAmount,
                    fee = null
                )
        }
    }
}
