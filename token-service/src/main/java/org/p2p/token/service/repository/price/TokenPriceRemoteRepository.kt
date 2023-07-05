package org.p2p.token.service.repository.price

import org.p2p.token.service.api.TokenServiceRepository
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.request.TokenServicePriceRequest
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.model.TokenServiceQueryResult
import org.p2p.token.service.model.successOrNull

internal class TokenPriceRemoteRepository(
    private val api: TokenServiceRepository,
    private val mapper: TokenServiceMapper
) : TokenPriceRepository {

    override suspend fun loadTokensPrice(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServicePrice>> {

        val queryRequest = mapper.toRequest(chain, addresses).let(::TokenServicePriceRequest)
        val queryResponse = api.launch(queryRequest).successOrNull().orEmpty()

        val tokensPrice = queryResponse.map { response ->
            val tokenServiceChain = mapper.fromNetwork(response.tokenServiceChainResponse)
            val tokenPrices = response.tokenServiceItemsResponse
                .mapNotNull { mapper.fromNetwork(tokenServiceChain,it) }

            TokenServiceQueryResult(networkChain = tokenServiceChain, items = tokenPrices)

        }
        return tokensPrice
    }
}
