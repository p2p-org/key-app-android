package org.p2p.ethereumkit.external.token

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.token.service.model.TokenServicePrice

class EthereumTokensInMemoryRepository : EthereumTokensLocalRepository {

    private val cachedTokens = MutableStateFlow<List<Token.Eth>>(emptyList())

    override suspend fun cacheTokens(tokens: List<Token.Eth>) {
        cachedTokens.value = tokens
    }

    override suspend fun updateTokensRate(tokensRate: List<TokenServicePrice>) {
        cachedTokens.value = cachedTokens.value.map { token ->
            val foundTokenRate = tokensRate.firstOrNull { it.address == token.publicKey }
            val totalInUsd = foundTokenRate?.usdRate?.let { token.total.times(it) }
            token.copy(totalInUsd = totalInUsd)
        }
    }

    override fun getTokensFlow(): StateFlow<List<Token.Eth>> {
        return cachedTokens
    }

    override fun getWalletTokens(): List<Token.Eth> {
        return cachedTokens.value
    }
}
