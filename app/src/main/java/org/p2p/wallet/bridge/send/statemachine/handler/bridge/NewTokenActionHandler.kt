package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class NewTokenActionHandler() : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.NewToken

    override fun handle(
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.NewToken
        emit(SendState.Event.TokenZero(action.token, null))
    }
}
