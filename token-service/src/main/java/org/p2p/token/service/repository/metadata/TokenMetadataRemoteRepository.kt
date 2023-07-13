package org.p2p.token.service.repository.metadata

import com.google.gson.Gson
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RpcApi
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.response.TokenListResponse
import org.p2p.token.service.model.UpdateTokenMetadataResult

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50

internal class TokenMetadataRemoteRepository(
    private val api: RpcApi,
    private val mapper: TokenServiceMapper,
    private val gson: Gson,
    private val urlProvider: NetworkServicesUrlProvider,
    private val dispatchers: CoroutineDispatchers
) : TokenMetadataRepository {

    private val tokenServiceUrl
        get() = urlProvider.loadTokenServiceEnvironment().baseServiceUrl

    override suspend fun loadTokensMetadata(lastModified: String?): UpdateTokenMetadataResult =
        withContext(dispatchers.io) {
            val response = try {
                api.getZipFile("${tokenServiceUrl}get_all_tokens_info", lastModified)
            } catch (e: HttpException) {
                return@withContext if (e.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    UpdateTokenMetadataResult.NoUpdate
                } else {
                    UpdateTokenMetadataResult.Error(e)
                }
            }

            val jsonResponse = gson.fromJson(response, TokenListResponse::class.java)

            val tokens = jsonResponse.tokens
                .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
                .flatMap { chunkedList -> chunkedList.map { mapper.fromNetwork(it) } }

            val metadata = TokensMetadataInfo(
                timestamp = jsonResponse.timestamp,
                tokens = tokens
            )

            UpdateTokenMetadataResult.NewMetadata(tokensMetadataInfo = metadata)
        }
}
