package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class NewTokenActionHandler() : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.NewToken

    override suspend fun handle(
        newAction: SendFeatureAction,
    ): SendState {
        val action = newAction as SendFeatureAction.NewToken
        return SendState.Event.TokenZero(action.token, null)
    }
}
