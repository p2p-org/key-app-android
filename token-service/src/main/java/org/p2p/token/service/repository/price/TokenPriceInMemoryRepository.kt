package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenPriceInMemoryRepository : TokenPriceLocalRepository {
    private val tokensPriceMap = mutableMapOf<TokenServiceNetwork, Map<String, TokenServicePrice>>()

    private val solanaTokensPriceFlow = MutableStateFlow<Map<String, TokenServicePrice>>(emptyMap())

    private val ethereumTokensPriceFlow = MutableStateFlow<Map<String, TokenServicePrice>>(emptyMap())

    override fun setTokensPrice(networkChain: TokenServiceNetwork, prices: List<TokenServicePrice>) {
        tokensPriceMap[networkChain] = prices.associateBy { it.address }
        publishToFlow(networkChain)
    }

    override fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, address: String): TokenServicePrice? {
        val tokenPrice = tokensPriceMap[networkChain]
        return tokenPrice?.get(address)
    }

    override fun attachToTokensPrice(networkChain: TokenServiceNetwork): StateFlow<Map<String, TokenServicePrice>> {
        return when (networkChain) {
            TokenServiceNetwork.SOLANA -> solanaTokensPriceFlow
            TokenServiceNetwork.ETHEREUM -> ethereumTokensPriceFlow
        }
    }

    private fun publishToFlow(networkChain: TokenServiceNetwork) {
        when (networkChain) {
            TokenServiceNetwork.ETHEREUM -> {
                ethereumTokensPriceFlow.tryEmit(tokensPriceMap[networkChain].orEmpty())
            }
            TokenServiceNetwork.SOLANA -> {
                solanaTokensPriceFlow.tryEmit(tokensPriceMap[networkChain].orEmpty())
            }
        }
    }
}
