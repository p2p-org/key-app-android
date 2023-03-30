package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class RefreshFeeActionHandler(
    private val repository: EthereumSendRepository,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override fun handle(
        newAction: SendFeatureAction,
    ): Flow<SendState> = flow {
        try {
            Timber.tag("_____EMIT").d("TRUE")
            emit(SendState.Loading.Fee(true))
            val action = newAction as SendFeatureAction.RefreshFee
            val fee = repository.getSendFee(
                userWallet = action.userWallet,
                recipient = action.recipient,
                mint = action.mintAddress,
                amount = action.amount
            )
            Timber.tag("_____EMIT").d("FALSE")
            emit(SendState.Event.UpdateFee(fee))
        } catch (e: Throwable) {
            emit(SendState.Exception.FeeLoading)
        } finally {
            emit(SendState.Loading.Fee(false))
            Timber.tag("_____EMIT").d("FINALLY")
        }
    }
}
