package org.p2p.wallet.pnl.repository

import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.gson.GsonProvider
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.pnl.api.PnlDataTimeSpan
import org.p2p.wallet.pnl.api.PnlResponseDeserializer
import org.p2p.wallet.pnl.api.PnlServiceApi
import org.p2p.wallet.pnl.models.PnlData

class PnlRemoteRepository(
    private val api: PnlServiceApi,
    private val dispatchers: CoroutineDispatchers
) : PnlRepository {

    override suspend fun getPnlData(
        userWallet: Base58String,
        tokenMints: List<Base58String>,
        timeSpan: PnlDataTimeSpan,
    ): PnlData = withContext(dispatchers.io) {
        val requestedTokenMints: List<String>? = tokenMints
            .map { it.base58Value }
            .ifEmpty { null }

        val params = buildMap {
            this["user_wallet"] = userWallet.base58Value
            // todo: backend decided to use null for a default duration
            //       currently it's 24 hours, something may change in the future
            this["since"] = timeSpan.sinceEpochSeconds
            this["token_mints"] = requestedTokenMints
        }

        val rpcRequest = RpcMapRequest(method = "get_pnl", params = params)
        val response = api.getAccountPnl(rpcRequest)
        val gson = GsonProvider()
            .withBuilder {
                registerTypeAdapter(PnlData::class.java, PnlResponseDeserializer())
            }
            .provide()

        gson.fromJson(response.result, PnlData::class.java)
    }
}
