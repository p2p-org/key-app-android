package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper

class AmountChangeActionHandler(
    private val mapper: SendBridgeStaticStateMapper,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.AmountChange

    override fun handle(
        newAction: SendFeatureAction,
    ): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.AmountChange
        TODO()
    }
}
