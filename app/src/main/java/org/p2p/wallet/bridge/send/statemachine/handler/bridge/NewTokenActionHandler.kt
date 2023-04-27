package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class NewTokenActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState.Static): Boolean =
        newEvent is SendFeatureAction.NewToken && newEvent.token is SendToken.Bridge

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.NewToken
        val newToken = action.token as SendToken.Bridge

        val state = SendState.Static.TokenZero(newToken, null)
        emit(state)
        transactionLoader.prepareTransaction(state).collect {
            emit(it)
        }
    }
}
