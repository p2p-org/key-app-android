package org.p2p.wallet.newsend.statemachine.handler

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.newsend.statemachine.model.SendToken

class InitFeatureActionHandler(
    private val initialToken: SendToken.Bridge,
    private val feeLoader: SendBridgeFeeLoader,
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
        stateFlow.value = SendState.Static.TokenZero(initialToken, null)
        feeLoader.updateFee(stateFlow)
    }
}
