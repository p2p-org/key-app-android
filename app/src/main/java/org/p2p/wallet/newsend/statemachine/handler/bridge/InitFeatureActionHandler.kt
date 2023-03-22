package org.p2p.wallet.newsend.statemachine.handler.bridge

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.newsend.statemachine.model.SendInitialData

class InitFeatureActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
    private val initialData: SendInitialData.Common,
) : SendActionHandler {

    override fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override suspend fun handle(
        stateFlow: MutableStateFlow<SendState>,
        staticState: SendState.Static,
        newAction: SendFeatureAction
    ) {
        val initialState = if (initialData.initialAmount == null) {
            SendState.Static.TokenZero(initialData.initialToken, null)
        } else {
            SendState.Static.TokenNotZero(initialData.initialToken, initialData.initialAmount)
        }
        stateFlow.value = initialState
        feeLoader.updateFee(stateFlow)
    }
}
