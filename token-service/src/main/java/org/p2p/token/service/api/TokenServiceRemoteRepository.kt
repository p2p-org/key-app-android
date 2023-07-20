package org.p2p.token.service.api

import com.google.gson.Gson
import timber.log.Timber
import java.net.URI
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.token.service.model.TokenServiceResult

private const val TAG = "BridgeRemoteRepository"

internal class TokenServiceRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider,
) : TokenServiceApiRepository {
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
            TokenServiceResult.Error(e)
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "failed request for ${request.method}")
            TokenServiceResult.Error(e)
        }
    }
}
