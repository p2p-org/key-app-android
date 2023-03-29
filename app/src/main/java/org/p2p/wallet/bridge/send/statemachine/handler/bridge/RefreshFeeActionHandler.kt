package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState

class RefreshFeeActionHandler(
    private val repository: EthereumSendRepository,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean =
        newEvent is SendFeatureAction.RefreshFee

    override suspend fun handle(newAction: SendFeatureAction): SendState {
        return try {
            val action = newAction as SendFeatureAction.RefreshFee
            val fee = repository.getSendFee(
                userWallet = action.userWallet,
                recipient = action.recipient,
                mint = action.mintAddress,
                amount = action.amount
            )
            SendState.Event.UpdateFee(fee)
        } catch (e: Throwable) {
            SendState.Exception.FeeLoading
        }
    }
}
