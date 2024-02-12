package org.p2p.ethereumkit.external.token

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.token.service.model.TokenServicePrice

class EthereumTokensInMemoryRepository : EthereumTokensLocalRepository {

    private val cachedTokens = MutableStateFlow<List<Token.Eth>>(emptyList())

    override suspend fun cacheTokens(newTokens: List<Token.Eth>) {
        val oldTokens = cachedTokens.value
        val validatedTokens = newTokens.map { newToken ->
            val oldToken = oldTokens.firstOrNull { newToken.tokenServiceAddress == it.tokenServiceAddress }

            val newTokenRate = newToken.rate ?: oldToken?.rate
            val newTotalInUsd = newTokenRate?.let { newToken.total.times(it) }
            newToken.copy(rate = newTokenRate, totalInUsd = newTotalInUsd)
        }
        cachedTokens.value = validatedTokens
    }

    override suspend fun updateTokensRate(tokensRate: List<TokenServicePrice>) {
        val newTokenRates = buildList<TokenServicePrice> {
            val nativePrice = tokensRate.firstOrNull { it.tokenAddress == ERC20Tokens.ETH.contractAddress }
            if (nativePrice != null) {
                this += nativePrice.copy(tokenAddress = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN)
            }
            this += tokensRate
        }
        cachedTokens.value = cachedTokens.value.map { token ->
            val foundTokenRate = newTokenRates.firstOrNull { it.tokenAddress == token.tokenServiceAddress }
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
