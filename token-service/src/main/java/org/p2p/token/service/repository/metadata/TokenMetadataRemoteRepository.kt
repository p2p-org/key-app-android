package org.p2p.token.service.repository.metadata

import com.google.gson.Gson
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.URI
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RpcApi
import org.p2p.core.token.TokenMetadata
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.response.TokenListResponse
import org.p2p.token.service.model.TokenMetadataResult

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50

internal class TokenMetadataRemoteRepository(
    private val api: RpcApi,
    private val mapper: TokenServiceMapper,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider
) : TokenMetadataRepository {

    private val tokenServiceStringUrl = urlProvider.loadTokenServiceEnvironment().baseServiceUrl
    private val tokenServiceUrl = URI(tokenServiceStringUrl)

    override suspend fun loadTokensMetadata(lastModified: String?): TokenMetadataResult {
        val response = try {
            api.getZipFile("${tokenServiceUrl}get_all_tokens_info", lastModified)
        } catch (e: HttpException) {
            return if (e.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                TokenMetadataResult.NoUpdate
            } else {
                TokenMetadataResult.Error(e)
            }
        }

        val jsonResponse = gson.fromJson(response, TokenListResponse::class.java)

        val tokens = jsonResponse.tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList -> chunkedList.map { mapper.fromNetwork(it) } }

        val metadata = TokenMetadata(
            timestamp = jsonResponse.timestamp,
            data = tokens
        )
        return TokenMetadataResult.NewMetadata(tokensMetadata = metadata)
    }
}
