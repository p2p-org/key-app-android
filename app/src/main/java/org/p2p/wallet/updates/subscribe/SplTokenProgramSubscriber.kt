package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.UpdateType

private const val SUBSCRIBE_METHOD_NAME = "programSubscribe"
private const val UNSUBSCRIBE_METHOD_NAME = "programUnsubscribe"
private const val PARAMS_NUMBER = "number"
private const val PARAMS_PROGRAM_ID = "program_id"

class SplTokenProgramSubscriber(
    private val updatesManager: SubscriptionUpdatesManager,
    tokenKeyProvider: TokenKeyProvider
) : SubscriptionUpdateSubscriber {

    private val request = RpcRequest(
        method = SUBSCRIBE_METHOD_NAME,
        params = listOf(
            SystemProgram.SPL_TOKEN_PROGRAM_ID.toBase58(),
            mapOf(
                "сommitment" to "сonfirmed",
                "encoding" to "base64",
                "filters" to listOf(
                    mapOf("dataSize" to TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH),
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
            updateType = UpdateType.SPL_TOKEN_PROGRAM_UPDATED
        )
    }

    override fun unSubscribe() {
        updatesManager.removeSubscription(cancelRequest)
    }
}
