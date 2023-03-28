package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData

class InitFeatureActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
    private val initialData: SendInitialData.Bridge,
) : SendActionHandler {

    override fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val initialState = if (initialData.initialAmount == null) {
            SendState.Static.TokenZero(initialData.initialToken, null)
        } else {
            SendState.Static.TokenNotZero(initialData.initialToken, initialData.initialAmount)
        }
        emit(initialState)
    }.flatMapMerge { feeLoader.updateFee(lastStaticState) }
}
