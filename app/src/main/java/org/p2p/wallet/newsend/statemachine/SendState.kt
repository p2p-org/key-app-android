package org.p2p.wallet.newsend.statemachine

import java.math.BigDecimal
import org.p2p.wallet.newsend.statemachine.model.SendFee
import org.p2p.wallet.newsend.statemachine.model.SendToken

sealed interface SendBridgeState

sealed interface SendState {

    sealed interface Loading : SendState, SendBridgeState {
        val lastStaticState: Static

        data class Fee(override val lastStaticState: Static) : Loading
    }

    sealed interface Exception : SendState, SendBridgeState {
        val lastStaticState: Static

        data class Other(
            override val lastStaticState: Static,
            val exception: kotlin.Exception,
        ) : Exception

        data class Feature(
            override val lastStaticState: Static,
            val featureException: SendFeatureException,
        ) : Exception
    }

    sealed interface Static : SendState {

        object Empty : Static, SendBridgeState

        data class TokenZero(
            val token: SendToken,
            val fee: SendFee? = null,
        ) : Static, SendBridgeState

        data class TokenNotZero(
            val token: SendToken,
            val amount: BigDecimal,
            val fee: SendFee? = null,
        ) : Static, SendBridgeState

        data class ReadyToSend(
            val token: SendToken,
            val fee: SendFee,
            val amount: BigDecimal,
        ) : Static, SendBridgeState
    }
}