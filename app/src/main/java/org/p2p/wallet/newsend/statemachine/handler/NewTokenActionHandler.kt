package org.p2p.wallet.newsend.statemachine.handler

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.newsend.statemachine.model.SendToken

class NewTokenActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState): Boolean =
        newEvent is SendFeatureAction.NewToken && newEvent.token is SendToken.Bridge

    override suspend fun handle(
        stateFlow: MutableStateFlow<SendState>,
        staticState: SendState.Static,
        newAction: SendFeatureAction
    ) {
        val action = newAction as SendFeatureAction.NewToken
        val newToken = action.token as SendToken.Bridge

        stateFlow.value = SendState.Static.TokenZero(newToken, null)
        feeLoader.updateFee(stateFlow)
    }
}
