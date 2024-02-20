package org.p2p.token.service.repository.metadata

import com.google.gson.Gson
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RpcApi
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.api.tokenservice.TokenServiceDataSource
import org.p2p.token.service.api.tokenservice.request.TokenServiceMetadataRequest
import org.p2p.token.service.api.tokenservice.response.TokenListResponse
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServiceQueryResult
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.model.successOrNull
import org.p2p.token.service.repository.mapper.TokenServiceMapper

private const val ALL_TOKENS_MAP_CHUNKED_COUNT = 50
private const val HEADER_LAST_MODIFIED = "last-modified"

internal class TokenMetadataRemoteRepository(
    private val api: RpcApi,
    private val tokenServiceRepository: TokenServiceDataSource,
    private val mapper: TokenServiceMapper,
    private val gson: Gson,
    private val urlProvider: NetworkServicesUrlProvider,
    private val dispatchers: CoroutineDispatchers
) : TokenMetadataRepository {

    private val tokenServiceUrl: String
        get() = urlProvider.loadTokenServiceEnvironment().baseServiceUrl

    override suspend fun loadSolTokensMetadata(
        ifModifiedSince: String?
    ): UpdateTokenMetadataResult = withContext(dispatchers.io) {
        val response = try {
            api.getZipFile("${tokenServiceUrl}get_all_tokens_info", ifModifiedSince)
        } catch (error: Throwable) {
            return@withContext handleError(error)
        }

        val responseBody = response.body() ?: return@withContext UpdateTokenMetadataResult.NoUpdate
        val jsonResponse = gson.fromJson(responseBody, TokenListResponse::class.java)

        val tokens = jsonResponse.tokens
            .chunked(ALL_TOKENS_MAP_CHUNKED_COUNT)
            .flatMap { chunkedList -> chunkedList.map { mapper.fromNetwork(it) } }

        val metadata = TokensMetadataInfo(
            timestamp = response.headers()[HEADER_LAST_MODIFIED],
            tokens = tokens
        )

        UpdateTokenMetadataResult.NewMetadata(remoteTokensMetadata = metadata)
    }

    private fun handleError(error: Throwable): UpdateTokenMetadataResult {
        return if (error is HttpException && error.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            UpdateTokenMetadataResult.NoUpdate
        } else {
            UpdateTokenMetadataResult.Error(error)
        }
    }

    override suspend fun loadTokensMetadata(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServiceMetadata>> {

        val queryRequest = mapper.toRequest(chain, addresses).let(::TokenServiceMetadataRequest)
        val queryResponse = tokenServiceRepository.launch(queryRequest).successOrNull().orEmpty()

        val tokensPrice = queryResponse.map { response ->
            val tokenServiceChain = mapper.fromNetwork(response.tokenServiceChainResponse)
            val tokenPrices = response.tokenServiceItemsResponse
                .map { mapper.fromNetwork(tokenServiceChain, it) }

            TokenServiceQueryResult(networkChain = tokenServiceChain, items = tokenPrices)
        }
        return tokensPrice
    }
}
