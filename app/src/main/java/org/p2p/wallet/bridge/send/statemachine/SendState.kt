package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.send.statemachine.model.SendFee

sealed interface SendState {

    sealed interface Loading : SendState {

        object Fee : Loading
    }

    sealed interface Exception : SendState {

        data class Other(
            val exception: kotlin.Exception,
        ) : Exception

        data class Feature(
            val featureException: SendFeatureException,
        ) : Exception
    }

    sealed interface Event : SendState {

        object Empty : Event

        data class TokenZero(
            val token: Token.Eth,
            val fee: SendFee? = null,
        ) : Event

        data class TokenNotZero(
            val token: Token.Eth,
            val amount: BigDecimal,
            val fee: SendFee? = null,
        ) : Event

        data class ReadyToSend(
            val token: Token.Eth,
            val fee: SendFee,
            val amount: BigDecimal,
        ) : Event
    }
}
