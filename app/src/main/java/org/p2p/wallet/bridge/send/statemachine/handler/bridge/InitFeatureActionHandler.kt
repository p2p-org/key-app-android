package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class InitFeatureActionHandler : SendActionHandler {

    override fun canHandle(
        newEvent: SendFeatureAction,
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override suspend fun handle(newAction: SendFeatureAction): SendState {
        val action = newAction as SendFeatureAction.InitFeature
        val token = action.initialToken
        val amount = action.initialAmount

        val state = if (amount == null) {
            SendState.Event.TokenZero(token, null)
        } else {
            SendState.Event.TokenNotZero(token, amount)
        }
        return state
    }
}
