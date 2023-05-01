package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.updates.UpdateType
import org.p2p.wallet.updates.UpdatesManager

class TokenProgramSubscriber(
    private val updatesManager: UpdatesManager
) : UpdateSubscriber {

    val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(PROGRAM_ID)
    )

    private val cancelRequest = RpcMapRequest(
        method = UNSUBSCRIBE_METHOD_NAME,
        params = mapOf(PARAMS_NUMBER to request.id)
    )

    override fun subscribe() {
        updatesManager.addSubscription(
            request = request,
            updateType = UpdateType.TOKEN_BALANCES_RECEIVED
        )
    }

    override fun unSubscribe() {
        updatesManager.removeSubscription(cancelRequest)
    }

    companion object {
        private const val PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        private const val SUBSCRIBE_METHOD_NAME = "programSubscribe"
        private const val UNSUBSCRIBE_METHOD_NAME = "programUnsubscribe"
        private const val PARAMS_NUMBER = "number"
        private const val PARAMS_PROGRAM_ID = "program_id"
    }
}
