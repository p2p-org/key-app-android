package org.p2p.token.service.repository.price

import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenPriceInMemoryRepository : TokenPriceLocalRepository {
    private val tokensPriceMap = mutableMapOf<TokenServiceNetwork, Map<String, TokenServicePrice>>()

    override fun setTokensPrice(networkChain: TokenServiceNetwork, prices: List<TokenServicePrice>) {
        tokensPriceMap[networkChain] = prices.associateBy { it.address }
    }

    override fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, address: String): TokenServicePrice? {
        val tokenPrice = tokensPriceMap[networkChain]
        return tokenPrice?.get(address)
    }
}
