package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class AmountChangeActionHandler() : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.AmountChange

    override suspend fun handle(newAction: SendFeatureAction): SendState {
        val action = newAction as SendFeatureAction.AmountChange
        return SendState.Event.Empty
    }
}
