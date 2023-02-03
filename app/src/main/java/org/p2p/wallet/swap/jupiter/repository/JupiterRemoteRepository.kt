package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.api.SwapJupiterTokensApi
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

class JupiterRemoteRepository(
    private val api: SwapJupiterTokensApi,
    private val mapper: JupiterRemoteMapper,
) : JupiterTokensRepository {

    override suspend fun getTokens(): List<JupiterSwapToken> =
        mapper.toJupiterToken(api.getTokens())
}
