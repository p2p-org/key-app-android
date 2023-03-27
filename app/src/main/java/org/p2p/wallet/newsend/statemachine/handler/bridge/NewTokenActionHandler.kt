package org.p2p.wallet.newsend.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.newsend.statemachine.model.SendToken

class NewTokenActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState): Boolean =
        newEvent is SendFeatureAction.NewToken && newEvent.token is SendToken.Common

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.NewToken
        val newToken = action.token as SendToken.Common

        emit(SendState.Static.TokenZero(newToken, null))
    }.flatMapMerge { feeLoader.updateFee(lastStaticState) }
}
