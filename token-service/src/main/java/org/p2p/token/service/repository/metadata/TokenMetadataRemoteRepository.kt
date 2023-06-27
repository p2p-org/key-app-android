package org.p2p.token.service.repository.metadata

import org.p2p.token.service.api.TokenServiceRepository
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.request.TokenServiceMetadataRequest
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServiceQueryResult
import org.p2p.token.service.model.successOrNull
import org.p2p.token.service.model.unwrap

class TokenMetadataRemoteRepository(
    private val api: TokenServiceRepository,
    private val mapper: TokenServiceMapper
) : TokenMetadataRepository {
    override suspend fun loadTokensMetadata(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServiceMetadata>> {

        val queryRequest =
            mapper.toRequest(chain, addresses).let(::TokenServiceMetadataRequest)
        val queryResponse = api.launch(queryRequest).successOrNull().orEmpty()

        val tokensMetadata = queryResponse.map { response ->
            val tokenServiceChain = mapper.fromNetwork(response.tokenServiceChainResponse)
            val tokenPrices = response.tokenServiceItemsResponse.map { mapper.fromNetwork(it) }

            TokenServiceQueryResult(networkChain = tokenServiceChain, items = tokenPrices)
        }
        return tokensMetadata
    }
}
