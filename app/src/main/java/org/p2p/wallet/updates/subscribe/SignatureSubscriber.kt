package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.updates.UpdateType
import org.p2p.wallet.updates.UpdatesManager

class SignatureSubscriber(
    private val txSignature: String,
    private val updatesManager: UpdatesManager
) : UpdateSubscriber {

    val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(txSignature)
    )

    private val cancelRequest = RpcMapRequest(
        method = UNSUBSCRIBE_METHOD_NAME,
        params = mapOf(PARAMS_NUMBER to request.id)
    )

    override fun subscribe() {
        updatesManager.addSubscription(request, UpdateType.SIGNATURE_RECEIVED)
    }

    override fun unSubscribe() {
        updatesManager.removeSubscription(cancelRequest)
    }

    companion object {
        private const val SUBSCRIBE_METHOD_NAME = "signatureSubscribe"
        private const val UNSUBSCRIBE_METHOD_NAME = "signatureUnsubscribe"
        private const val PARAMS_NUMBER = "number"
    }
}
