package org.p2p.wallet.updates.subscribe

import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.SocketSubscriptionUpdateType
import org.p2p.wallet.updates.SubscriptionUpdatesManager

private const val SUBSCRIBE_METHOD_NAME = "programSubscribe"
private const val UNSUBSCRIBE_METHOD_NAME = "programUnsubscribe"
private const val PARAMS_NUMBER = "number"

class Token2022ProgramSubscriber(
    private val updatesManager: SubscriptionUpdatesManager,
    private val tokenKeyProvider: TokenKeyProvider
) : SubscriptionUpdateSubscriber {

    private var request: RpcRequest? = null

    private fun createRequest(): RpcRequest {
        return RpcRequest(
            method = SUBSCRIBE_METHOD_NAME,
            params = listOf(
                SystemProgram.TOKEN2022_PROGRAM_ID,
                mapOf(
                    "commitment" to ConfirmationStatus.CONFIRMED.value,
                    "encoding" to Encoding.BASE64.encoding,
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
    }

    override fun subscribe() {
        request = createRequest()
        updatesManager.addSubscription(
            request = request ?: return,
            updateType = SocketSubscriptionUpdateType.SPL_TOKEN_PROGRAM_UPDATED
        )
    }

    override fun unSubscribe() {
        val id = request?.id ?: return
        val cancelRequest = RpcMapRequest(
            method = UNSUBSCRIBE_METHOD_NAME,
            params = mapOf(PARAMS_NUMBER to id)
        )
        updatesManager.removeSubscription(cancelRequest)
    }
}
