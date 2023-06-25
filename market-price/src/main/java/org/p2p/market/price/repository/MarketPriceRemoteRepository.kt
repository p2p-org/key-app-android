package org.p2p.market.price.repository

import com.google.gson.Gson
import timber.log.Timber
import java.net.URI
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.market.price.model.MarketPriceResult

private const val TAG = "BridgeRemoteRepository"

class MarketPriceRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider,
) : MarketPriceRepository {
    private val bridgeStringUrl = urlProvider.loadBridgesServiceEnvironment().baseUrl
    private val bridgeUrl = URI(bridgeStringUrl)

    override suspend fun <P, T> launch(request: JsonRpc<P, T>): MarketPriceResult.Success<T> {
        try {
            val requestGson = gson.toJson(request)
            val response = api.launch(bridgeUrl, jsonRpc = requestGson)
            val result = request.parseResponse(response, gson)
            return MarketPriceResult.Success(result)
        } catch (e: JsonRpc.ResponseError.RpcError) {
            Timber.tag(TAG).i(e, "failed request for ${request.method}")
            Timber.tag(TAG).i("Error body message ${e.error.message}")
            val errorCode = e.error.code
            throw e
        }
    }
}
