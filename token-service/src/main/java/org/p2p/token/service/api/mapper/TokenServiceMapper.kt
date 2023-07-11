package org.p2p.token.service.api.mapper

import org.p2p.token.service.api.request.TokenServiceItemRequest
import org.p2p.token.service.api.request.TokenServiceQueryRequest
import org.p2p.token.service.api.response.TokenItemMetadataResponse
import org.p2p.token.service.api.response.TokenItemPriceResponse
import org.p2p.token.service.api.response.TokenRateResponse
import org.p2p.token.service.api.response.TokenServiceNetworkResponse
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

class TokenServiceMapper {

    internal fun fromNetwork(response: TokenItemMetadataResponse): TokenServiceMetadata {
        return TokenServiceMetadata(
            address = response.address,
            symbol = response.symbol,
            logoUrl = response.logoUrl,
            decimals = response.decimals,
            price = fromNetwork(response.price)
        )
    }

    internal fun fromNetwork(tokenServiceNetwork: TokenServiceNetwork,response: TokenItemPriceResponse): TokenServicePrice? {
        val tokenRate = response.price ?: return null
        return TokenServicePrice(
            address = response.tokenAddress,
            rate = fromNetwork(tokenRate),
            network = tokenServiceNetwork
        )
    }

    internal fun fromNetwork(response: TokenServiceNetworkResponse): TokenServiceNetwork {
        return when (response) {
            TokenServiceNetworkResponse.SOLANA -> TokenServiceNetwork.SOLANA
            TokenServiceNetworkResponse.ETHEREUM -> TokenServiceNetwork.ETHEREUM
        }
    }

    internal fun toNetwork(domain: TokenServiceNetwork): TokenServiceNetworkResponse {
        return when (domain) {
            TokenServiceNetwork.SOLANA -> TokenServiceNetworkResponse.SOLANA
            TokenServiceNetwork.ETHEREUM -> TokenServiceNetworkResponse.ETHEREUM
        }
    }

    internal fun toRequest(chain: TokenServiceNetwork, tokenAddresses: List<String>): TokenServiceQueryRequest {
        return TokenServiceQueryRequest(
            query = listOf(
                TokenServiceItemRequest(
                    chainId = toNetwork(chain),
                    addresses = tokenAddresses
                )
            )
        )
    }

    internal fun fromNetwork(response: TokenRateResponse): TokenRate {
        return TokenRate(usd = response.usd)
    }
}
