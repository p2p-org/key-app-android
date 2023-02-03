package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterTokensApi
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterToken
import kotlinx.coroutines.withContext

class JupiterSwapTokenRepository(
    private val api: SwapJupiterTokensApi,
    private val dispatchers: CoroutineDispatchers,
) : SwapTokensRepository {

    override suspend fun getTokens(): List<JupiterToken> =
        withContext(dispatchers.io) {
            api.getTokens().toJupiterToken()
        }

    private fun List<JupiterTokenResponse>.toJupiterToken(): List<JupiterToken> = map { respons ->
        JupiterToken(
            address = respons.address,
            chainId = respons.chainId,
            decimals = respons.decimals,
            extensions = respons.extensions,
            logoURI = respons.logoURI,
            name = respons.name,
            symbol = respons.symbol,
            tags = respons.tags,
        )
    }
}
