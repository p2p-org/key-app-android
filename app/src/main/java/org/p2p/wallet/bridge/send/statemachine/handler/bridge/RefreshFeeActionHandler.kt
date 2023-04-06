package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader

class RefreshFeeActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState.Static): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = transactionLoader.prepareTransaction(lastStaticState)
}
