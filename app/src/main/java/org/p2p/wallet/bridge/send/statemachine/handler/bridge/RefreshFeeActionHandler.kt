package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.bridge.send.statemachine.BridgeSendActionHandler
import org.p2p.wallet.bridge.send.statemachine.BridgeSendAction
import org.p2p.wallet.bridge.send.statemachine.BridgeSendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.lastStaticState

class RefreshFeeActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
) : BridgeSendActionHandler {

    override fun canHandle(newEvent: BridgeSendAction, staticState: BridgeSendState.Static): Boolean =
        newEvent is BridgeSendAction.RefreshFee

    override fun handle(
        currentState: BridgeSendState,
        newAction: BridgeSendAction
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
