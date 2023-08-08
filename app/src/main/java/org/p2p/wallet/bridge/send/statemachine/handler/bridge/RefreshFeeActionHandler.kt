package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.BridgeSendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.lastStaticState

class RefreshFeeActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: BridgeSendState.Static): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override fun handle(
        currentState: BridgeSendState,
        newAction: SendFeatureAction
    ): Flow<BridgeSendState> {
        val lastStaticState = currentState.lastStaticState
        val lastStateAmount = if (currentState is BridgeSendState.Exception.Feature) {
            currentState.featureException.amount
        } else {
            null
        }
        return transactionLoader.prepareTransaction(lastStaticState, lastStateAmount)
    }
}
