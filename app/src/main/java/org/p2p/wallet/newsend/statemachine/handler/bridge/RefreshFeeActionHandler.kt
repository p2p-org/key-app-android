package org.p2p.wallet.newsend.statemachine.handler.bridge

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader

class RefreshFeeActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override suspend fun handle(
        stateFlow: MutableStateFlow<SendState>,
        staticState: SendState.Static,
        newAction: SendFeatureAction
    ) {
        val action = newAction as SendFeatureAction.RefreshFee
        feeLoader.updateFee(stateFlow)
    }
}
