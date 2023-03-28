package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class InitFeatureActionHandler : SendActionHandler {

    override fun canHandle(
        newEvent: SendFeatureAction,
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override fun handle(
        newAction: SendFeatureAction,
    ): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.InitFeature
        val token = action.initialToken
        val amount = action.initialAmount

        val state = if (amount == null) {
            SendState.Event.TokenZero(token, null)
        } else {
            SendState.Event.TokenNotZero(token, amount)
        }
        emit(state)
    }
}
