package org.p2p.token.service.repository

import org.p2p.token.service.api.TokenServiceApiRepository
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.request.TokenServiceItemRequest
import org.p2p.token.service.api.request.TokenServiceMetadataRequest
import org.p2p.token.service.api.request.TokenServicePriceRequest
import org.p2p.token.service.api.request.TokenServiceQueryRequest
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.model.TokenServiceQueryResult
import org.p2p.token.service.model.unwrap

class TokenServiceRemoteRepository(
    private val api: TokenServiceApiRepository,
    private val mapper: TokenServiceMapper
) : TokenServiceRepository {

    override suspend fun loadTokensPrice(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServicePrice>> {

        val queryRequest = createRequest(chain, addresses).let(::TokenServicePriceRequest)
        val queryResponse = api.launch(queryRequest).unwrap().orEmpty()

        val tokensPrice = queryResponse.map { response ->
            val tokenServiceChain = mapper.fromNetwork(response.tokenServiceChainResponse)
            val tokenPrices = response.tokenServiceItemsResponse.map { mapper.fromNetwork(it) }

            TokenServiceQueryResult(networkChain = tokenServiceChain, items = tokenPrices)
        }
        return tokensPrice
    }

    override suspend fun loadTokensMetadata(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServiceMetadata>> {

        val queryRequest = createRequest(chain, addresses).let(::TokenServiceMetadataRequest)
        val queryResponse = api.launch(queryRequest).unwrap().orEmpty()

        val tokensMetadata = queryResponse.map { response ->
            val tokenServiceChain = mapper.fromNetwork(response.tokenServiceChainResponse)
            val tokenPrices = response.tokenServiceItemsResponse.map { mapper.fromNetwork(it) }

            TokenServiceQueryResult(networkChain = tokenServiceChain, items = tokenPrices)
        }
        return tokensMetadata
    }

    private fun createRequest(chain: TokenServiceNetwork, tokenAddresses: List<String>): TokenServiceQueryRequest {
        return TokenServiceQueryRequest(
            tokenAddresses.map {
                TokenServiceItemRequest(
                    chainId = mapper.toNetwork(chain),
                    addresses = tokenAddresses
                )
            }
        )
    }
}
