package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

class InitFeatureActionHandler(
    private val transactionLoader: SendBridgeTransactionLoader,
    private val initialData: SendInitialData.Bridge,
    private val interactor: BridgeSendInteractor,
) : SendActionHandler {

    override fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState.Static
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override fun handle(
        currentState: SendState,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val userTokens = interactor.supportedSendTokens()
        val initialToken = initialData.initialToken ?: SendToken.Bridge(userTokens.first())

        val tokenState = if (initialData.initialAmount == null) {
            SendState.Static.TokenZero(initialToken, null)
        } else {
            SendState.Static.TokenNotZero(initialToken, initialData.initialAmount)
        }
        emit(tokenState)
        transactionLoader.prepareTransaction(tokenState)
            .collect {
                emit(it)
            }
    }
}
