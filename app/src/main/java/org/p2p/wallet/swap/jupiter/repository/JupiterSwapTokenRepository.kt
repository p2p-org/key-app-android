package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterTokensApi
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.SwapToken
import org.p2p.wallet.swap.jupiter.repository.model.SwapTokenExtensions
import org.p2p.wallet.utils.toBase58Instance
import kotlinx.coroutines.withContext

class JupiterSwapTokenRepository(
    private val api: SwapJupiterTokensApi,
    private val dispatchers: CoroutineDispatchers,
) : SwapTokensRepository {

    override suspend fun getTokens(): List<SwapToken> =
        withContext(dispatchers.io) {
            api.getTokens().toJupiterToken()
        }

    private fun List<JupiterTokenResponse>.toJupiterToken(): List<SwapToken> = map { respons ->
        SwapToken(
            address = respons.address.toBase58Instance(),
            chainId = respons.chainId,
            decimals = respons.decimals,
            extensions = SwapTokenExtensions(respons.extensions.coingeckoId),
            logoUri = respons.logoUri,
            name = respons.name,
            symbol = respons.symbol,
            tags = respons.tags,
        )
    }
}
