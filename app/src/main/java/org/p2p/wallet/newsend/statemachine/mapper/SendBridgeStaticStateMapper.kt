package org.p2p.wallet.newsend.statemachine.mapper

import java.math.BigDecimal
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.model.SendFee

class SendBridgeStaticStateMapper {

    fun updateFee(oldState: SendState.Static, newFee: SendFee.Common): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.ReadyToSend -> oldState.copy(fee = newFee)
            is SendState.Static.TokenNotZero -> oldState.copy(fee = newFee)
            is SendState.Static.TokenZero -> oldState.copy(fee = newFee)
        }
    }

    fun updateInputAmount(oldState: SendState.Static, newAmount: BigDecimal): SendState.Static {
        return when (oldState) {
            SendState.Static.Empty -> oldState
            is SendState.Static.ReadyToSend -> oldState.copy(amount = newAmount)
            is SendState.Static.TokenNotZero -> oldState.copy(amount = newAmount)

            is SendState.Static.TokenZero ->
                if (oldState.fee == null) {
                    SendState.Static.TokenNotZero(
                        token = oldState.token,
                        amount = newAmount,
                        fee = null
                    )
                } else {
                    SendState.Static.ReadyToSend(
                        token = oldState.token,
                        amount = newAmount,
                        fee = oldState.fee
                    )
                }
        }
    }
}
