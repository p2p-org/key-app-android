package org.p2p.wallet.bridge.send.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee

class SendBridgeStaticStateMapper {

    fun updateFee(oldState: SendState.Event, newFee: SendFee.Bridge): SendState.Event {
        return when (oldState) {
            SendState.Event.Empty -> oldState
            is SendState.Event.ReadyToSend -> oldState.copy(fee = newFee)
            is SendState.Event.TokenNotZero -> oldState.copy(fee = newFee)
            is SendState.Event.TokenZero -> oldState.copy(fee = newFee)
        }
    }

    fun updateInputAmount(oldState: SendState.Event, newAmount: BigDecimal): SendState.Event {
        return when (oldState) {
            SendState.Event.Empty -> oldState
            is SendState.Event.ReadyToSend -> oldState.copy(amount = newAmount)
            is SendState.Event.TokenNotZero -> oldState.copy(amount = newAmount)

            is SendState.Event.TokenZero ->
                if (oldState.fee == null) {
                    SendState.Event.TokenNotZero(
                        token = oldState.token,
                        amount = newAmount,
                        fee = null
                    )
                } else {
                    SendState.Event.ReadyToSend(
                        token = oldState.token,
                        amount = newAmount,
                        fee = oldState.fee
                    )
                }
        }
    }
}
