package org.p2p.wallet.swap.jupiter.repository.tokens

import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toBase58Instance

internal class JupiterSwapTokensRemoteRepository(
    private val api: SwapJupiterApi,
    private val swapTokensLocalRepository: JupiterSwapTokensLocalRepository,
    private val dispatchers: CoroutineDispatchers,
    private val userRepository: UserLocalRepository,
) : JupiterSwapTokensRepository {

    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.io) {
        swapTokensLocalRepository.getCachedTokens()
            ?: fetchTokensFromRemote()
                .also(swapTokensLocalRepository::setCachedTokens)
    }

    private suspend fun fetchTokensFromRemote(): List<JupiterSwapToken> {
        return api.getSwapTokens().toJupiterToken()
    }

    private fun List<JupiterTokenResponse>.toJupiterToken(): List<JupiterSwapToken> = map { response ->
        val tokenLogoUri = userRepository.findTokenData(mintAddress = response.address)?.iconUrl
        JupiterSwapToken(
            tokenMint = response.address.toBase58Instance(),
            chainId = response.chainId,
            decimals = response.decimals,
            coingeckoId = response.extensions?.coingeckoId,
            logoUri = tokenLogoUri,
            tokenName = response.name,
            tokenSymbol = response.symbol,
            tags = response.tags,
        )
    }
}
