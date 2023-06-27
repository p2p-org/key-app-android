package org.p2p.token.service.api

import com.google.gson.Gson
import timber.log.Timber
import java.net.URI
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.token.service.model.TokenServiceResult

private const val TAG = "BridgeRemoteRepository"

class TokenServiceRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider,
) : TokenServiceRepository {
    private val tokenServiceStringUrl = urlProvider.loadTokenServiceEnvironment().baseServiceUrl
    private val tokenServiceUrl = URI(tokenServiceStringUrl)

    override suspend fun <P, T> launch(request: JsonRpc<P, T>): TokenServiceResult<T> {
        return try {
            val requestGson = gson.toJson(request)
            val response = api.launch(tokenServiceUrl, jsonRpc = requestGson)
            val result = request.parseResponse(response, gson)
            TokenServiceResult.Success(result)
        } catch (e: JsonRpc.ResponseError.RpcError) {
            Timber.tag(TAG).i(e, "failed request for ${request.method}")
            Timber.tag(TAG).i("Error body message ${e.error.message}")
            TokenServiceResult.Error(e)
        }
    }
}
