package org.p2p.ethereumkit.external.token

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.token.service.model.TokenServicePrice

class EthereumTokensInMemoryRepository : EthereumTokensLocalRepository {

    private val cachedTokens = MutableStateFlow<List<Token.Eth>>(emptyList())

    override suspend fun cacheTokens(tokens: List<Token.Eth>) {
        val oldTokens = cachedTokens.value
        val newTokens = tokens.map { token ->
            val oldToken = oldTokens.firstOrNull { token.tokenServiceAddress == it.tokenServiceAddress }

            val newTokenRate = token.rate ?: oldToken?.rate
            val newTotalInUsd = newTokenRate?.let { token.total.times(it) }
            token.copy(rate = newTokenRate, totalInUsd = newTotalInUsd)
        }
        cachedTokens.value = newTokens
    }

    override suspend fun updateTokensRate(tokensRate: List<TokenServicePrice>) {
        val newTokenRates = buildList<TokenServicePrice> {
            val nativePrice = tokensRate.firstOrNull { it.address == ERC20Tokens.ETH.contractAddress }
            if (nativePrice != null) {
                this += nativePrice.copy(address = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN)
            }
            this += tokensRate
        }
        cachedTokens.value = cachedTokens.value.map { token ->
            val foundTokenRate = newTokenRates.firstOrNull { it.address == token.tokenServiceAddress }
            val totalInUsd = foundTokenRate?.usdRate?.let { token.total.times(it) }
            token.copy(totalInUsd = totalInUsd, rate = foundTokenRate?.usdRate)
        }
    }

    override fun getTokensFlow(): StateFlow<List<Token.Eth>> {
        return cachedTokens
    }

    override fun getWalletTokens(): List<Token.Eth> {
        return cachedTokens.value
    }
}
