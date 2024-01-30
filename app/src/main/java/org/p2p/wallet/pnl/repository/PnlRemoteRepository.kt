package org.p2p.wallet.pnl.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.gson.GsonProvider
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.pnl.api.PnlDataTimeSpan
import org.p2p.wallet.pnl.api.PnlResponseDeserializer
import org.p2p.wallet.pnl.api.PnlServiceApi
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.storage.PnlStorageContract

class PnlRemoteRepository(
    private val api: PnlServiceApi,
    private val storage: PnlStorageContract,
) : PnlRepository {

    override suspend fun getPnlData(
        timeSpan: PnlDataTimeSpan,
        userWallet: Base58String,
        tokenMints: List<Base58String>
    ): PnlData = withContext(Dispatchers.IO) {
        storage.getOrCache {
            getPnlDataInternal(timeSpan, userWallet, tokenMints)
        }
    }

    private suspend fun getPnlDataInternal(
        requestTime: PnlDataTimeSpan,
        userWallet: Base58String,
        tokenMints: List<Base58String>
    ): PnlData {
        val params = buildMap {
            put("user_wallet", userWallet.base58Value)
            val sinceOffset = when (requestTime) {
                // unix timestamp
                PnlDataTimeSpan.LAST_24_HOURS -> daysBackToTimestamp(1)
                PnlDataTimeSpan.LAST_7_DAYS -> daysBackToTimestamp(7)
                PnlDataTimeSpan.LAST_30_DAYS -> daysBackToTimestamp(30)
                PnlDataTimeSpan.LAST_90_DAYS -> daysBackToTimestamp(90)
                PnlDataTimeSpan.LAST_365_DAYS -> daysBackToTimestamp(365)
                PnlDataTimeSpan.ALL_TIME -> null
            }
            put("since", sinceOffset)
            val requestedTokenMints: List<String>? = tokenMints
                .map { it.base58Value }
                .ifEmpty { null }
            put("token_mints", requestedTokenMints)
        }

        val rpcRequest = RpcMapRequest("get_pnl", params)
        val response = api.getAccountPnl(rpcRequest)
        val gson = GsonProvider()
            .withBuilder {
                registerTypeAdapter(PnlData::class.java, PnlResponseDeserializer())
            }
            .provide()

        return gson.fromJson(response.result, PnlData::class.java)
    }

    private fun daysBackToTimestamp(days: Int): Long {
        return System.currentTimeMillis() - days * 24 * 60 * 60 * 1000
    }
}
