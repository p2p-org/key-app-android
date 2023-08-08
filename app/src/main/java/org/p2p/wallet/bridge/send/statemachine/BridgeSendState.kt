package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

sealed interface SendBridgeState

sealed interface BridgeSendState {

    sealed interface Loading : BridgeSendState, SendBridgeState {
        val lastStaticState: Static

        data class Fee(override val lastStaticState: Static) : Loading
    }

    sealed interface Exception : BridgeSendState, SendBridgeState {
        val lastStaticState: Static

        data class Other(
            override val lastStaticState: Static,
            val exception: Throwable,
        ) : Exception

        data class Feature(
            override val lastStaticState: Static,
            val featureException: SendFeatureException,
        ) : Exception
    }

    sealed interface Static : BridgeSendState {

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
            val sendTransaction: BridgeSendTransaction,
        ) : Static, SendBridgeState
    }
}
