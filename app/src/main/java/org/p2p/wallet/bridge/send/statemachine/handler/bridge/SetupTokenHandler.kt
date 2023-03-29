package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class SetupTokenHandler :
    SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean {
        return newEvent is SendFeatureAction.SetupInitialToken
    }

    override fun handle(newAction: SendFeatureAction): Flow<SendState> = flow {
    }
}
