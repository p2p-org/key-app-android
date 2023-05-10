package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.UpdateType

private const val SUBSCRIBE_METHOD_NAME = "accountSubscribe"
private const val UNSUBSCRIBE_METHOD_NAME = "accountUnsubscribe"
private const val PARAMS_NUMBER = "number"

class BalanceUpdateSubscriber(
    private val socketUpdatesManager: SubscriptionUpdatesManager,
    tokenKeyProvider: TokenKeyProvider
) : SubscriptionUpdateSubscriber {

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
            updateType = UpdateType.SOL_TOKEN_UPDATED
        )
    }

    override fun unSubscribe() {
        socketUpdatesManager.removeSubscription(cancelRequest)
    }
}
