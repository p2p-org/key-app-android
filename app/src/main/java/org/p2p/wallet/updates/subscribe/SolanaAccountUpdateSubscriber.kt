package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.SocketSubscriptionUpdateType
import org.p2p.wallet.updates.SubscriptionUpdatesManager

private const val SUBSCRIBE_METHOD_NAME = "accountSubscribe"
private const val UNSUBSCRIBE_METHOD_NAME = "accountUnsubscribe"
private const val PARAMS_NUMBER = "number"

class SolanaAccountUpdateSubscriber(
    private val socketUpdatesManager: SubscriptionUpdatesManager,
    private val tokenKeyProvider: TokenKeyProvider
) : SubscriptionUpdateSubscriber {

    private var request: RpcRequest? = null
    private fun createRequest(): RpcRequest {
        return RpcRequest(
            method = SUBSCRIBE_METHOD_NAME,
            params = listOf(
                tokenKeyProvider.publicKey,
                mapOf(
                    "commitment" to ConfirmationStatus.CONFIRMED.value,
                    "encoding" to Encoding.BASE64.encoding
                )
            )
        )
    }

    override fun subscribe() {
        request = createRequest()
        socketUpdatesManager.addSubscription(
            request = request ?: return,
            updateType = SocketSubscriptionUpdateType.SOL_TOKEN_UPDATED
        )
    }

    override fun unSubscribe() {
        val id = request?.id ?: return
        val cancelRequest = RpcMapRequest(
            method = UNSUBSCRIBE_METHOD_NAME,
            params = mapOf(PARAMS_NUMBER to id)
        )
        socketUpdatesManager.removeSubscription(cancelRequest)
    }
}
