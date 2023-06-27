package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.StateFlow
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenPriceLocalRepository {
    fun setTokensPrice(networkChain: TokenServiceNetwork, prices: List<TokenServicePrice>)
    fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, address: String): TokenServicePrice?
    fun attachToTokensPrice(networkChain: TokenServiceNetwork): StateFlow<Map<String,TokenServicePrice>>
}
