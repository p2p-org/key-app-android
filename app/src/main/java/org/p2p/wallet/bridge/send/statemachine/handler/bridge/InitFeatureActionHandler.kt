package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.statemachine.BridgeSendActionHandler
import org.p2p.wallet.bridge.send.statemachine.BridgeSendAction
import org.p2p.wallet.bridge.send.statemachine.BridgeSendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.model.BridgeSendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class InitFeatureActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
    private val initialData: BridgeSendInitialData.Bridge,
    private val interactor: BridgeSendInteractor,
) : BridgeSendActionHandler {

    override fun canHandle(
        newEvent: BridgeSendAction,
        staticState: BridgeSendState.Static
    ): Boolean = newEvent is BridgeSendAction.InitFeature

    override fun handle(
        currentState: BridgeSendState,
        newAction: BridgeSendAction
    ): Flow<BridgeSendState> = flow {
        val userTokens = interactor.supportedSendTokens()
        val initialToken = initialData.initialToken ?: SendToken.Bridge(userTokens.first())

        val tokenState = if (initialData.initialAmount == null) {
            BridgeSendState.Static.TokenZero(initialToken, null)
        } else {
            BridgeSendState.Static.TokenNotZero(initialToken, initialData.initialAmount)
        }
        emit(tokenState)
        transactionLoader.prepareTransaction(tokenState)
            .collect {
                emit(it)
            }
    }
}
