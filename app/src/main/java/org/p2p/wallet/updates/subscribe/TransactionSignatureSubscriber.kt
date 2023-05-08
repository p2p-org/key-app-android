package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.UpdateType

private const val SUBSCRIBE_METHOD_NAME = "signatureSubscribe"
private const val UNSUBSCRIBE_METHOD_NAME = "signatureUnsubscribe"
private const val PARAMS_NUMBER = "number"

class TransactionSignatureSubscriber(
    txSignature: String,
    private val updatesManager: SubscriptionUpdatesManager
) : SubscriptionUpdateSubscriber {

    private val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(txSignature)
    )

    private val cancelRequest = RpcMapRequest(
        method = UNSUBSCRIBE_METHOD_NAME,
        params = mapOf(PARAMS_NUMBER to request.id)
    )

    override fun subscribe() {
        updatesManager.addSubscription(request, UpdateType.TX_SIGNATURE_UPDATED)
    }

    override fun unSubscribe() {
        updatesManager.removeSubscription(cancelRequest)
    }
}
