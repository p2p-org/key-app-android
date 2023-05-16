package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.lastStaticState
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class NewTokenActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState.Static): Boolean =
        newEvent is SendFeatureAction.NewToken && newEvent.token is SendToken.Bridge

    override fun handle(
        currentState: SendState,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val lastStaticState = currentState.lastStaticState
        val action = newAction as SendFeatureAction.NewToken
        val newToken = action.token as SendToken.Bridge

        val state = SendState.Static.TokenZero(newToken, lastStaticState.fee)
        emit(state)
        val lastStateAmount = if (currentState is SendState.Exception.Feature) {
            currentState.featureException.amount
        } else {
            null
        }
        transactionLoader.prepareTransaction(state, lastStateAmount).collect {
            emit(it)
        }
    }
}
