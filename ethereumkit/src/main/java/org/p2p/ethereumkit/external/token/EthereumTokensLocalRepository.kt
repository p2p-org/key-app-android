package org.p2p.ethereumkit.external.token

import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.token.Token
import org.p2p.token.service.model.TokenServicePrice

interface EthereumTokensLocalRepository {
    suspend fun cacheTokens(tokens: List<Token.Eth>)
    suspend fun updateTokensRate(tokensRate: List<TokenServicePrice>)
    fun getTokensFlow(): StateFlow<List<Token.Eth>>
}
