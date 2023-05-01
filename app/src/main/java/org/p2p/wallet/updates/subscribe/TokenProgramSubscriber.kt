package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.UpdateType
import org.p2p.wallet.updates.UpdatesManager

class TokenProgramSubscriber(
    private val updatesManager: UpdatesManager,
    private val tokenKeyProvider: TokenKeyProvider
) : UpdateSubscriber {

    val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(
            PROGRAM_ID,
            mapOf(
                "сommitment" to "сonfirmed",
                "encoding" to "base64",
                "filters" to listOf(
                    mapOf("dataSize" to 165),
                    mapOf(
                        "memcmp" to mapOf(
                            "offset" to 32,
                            "bytes" to tokenKeyProvider.publicKey
                        )
                    )
                )
            )
        )
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
