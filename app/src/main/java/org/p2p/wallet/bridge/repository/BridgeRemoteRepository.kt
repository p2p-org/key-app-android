package org.p2p.wallet.bridge.repository

import com.google.gson.Gson
import timber.log.Timber
import java.net.URI
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.bridge.api.mapper.BridgeServiceErrorMapper
import org.p2p.wallet.bridge.model.BridgeResult

private const val TAG = "BridgeRemoteRepository"

class BridgeRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    private val errorMapper: BridgeServiceErrorMapper,
    private val urlProvider: NetworkServicesUrlProvider,
) : BridgeRepository {
    private val bridgeUrl: URI
        get() = URI(urlProvider.loadBridgesServiceEnvironment().baseUrl)

    override suspend fun <P, T> launch(request: JsonRpc<P, T>): BridgeResult.Success<T> {
        try {
            val requestGson = gson.toJson(request)
            val response = api.launch(bridgeUrl, jsonRpc = requestGson)
            val result = request.parseResponse(response, gson)
            return BridgeResult.Success(result)
        } catch (e: JsonRpc.ResponseError.RpcError) {
            Timber.tag(TAG).i(e, "failed request for ${request.method}")
            Timber.tag(TAG).i("Error body message ${e.error.message}")
            val errorCode = e.error.code
            throw errorMapper.parseError(errorCode)
        }
    }
}
