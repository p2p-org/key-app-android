package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.UpdateType
import org.p2p.wallet.updates.UpdatesManager

class BalanceUpdateSubscriber(
    private val socketUpdatesManager: UpdatesManager,
    tokenKeyProvider: TokenKeyProvider
) : UpdateSubscriber {

    private val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(
            tokenKeyProvider.publicKey,
            mapOf(
                "Commitment" to "Confirmed",
                "encoding" to "base64"
            )
        )
    )

    private val cancelRequest = RpcMapRequest(
        method = UNSUBSCRIBE_METHOD_NAME,
        params = mapOf(PARAMS_NUMBER to request.id)
    )

    override fun subscribe() {
        socketUpdatesManager.addSubscription(
            request = request,
            updateType = UpdateType.BALANCE_RECEIVED
        )
    }

    override fun unSubscribe() {
        socketUpdatesManager.removeSubscription(cancelRequest)
    }

    companion object {
        private const val SUBSCRIBE_METHOD_NAME = "accountSubscribe"
        private const val UNSUBSCRIBE_METHOD_NAME = "accountUnsubscribe"
        private const val PARAMS_NUMBER = "number"
    }
}
