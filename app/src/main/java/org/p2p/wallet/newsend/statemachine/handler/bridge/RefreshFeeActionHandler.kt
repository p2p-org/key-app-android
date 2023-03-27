package org.p2p.wallet.newsend.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader

class RefreshFeeActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = feeLoader.updateFee(lastStaticState)
}
