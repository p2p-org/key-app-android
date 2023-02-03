package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.toBase58Instance

class JupiterRemoteMapper {

    fun toJupiterToken(list: List<JupiterTokenResponse>): List<JupiterSwapToken> = list.map { response ->
        JupiterSwapToken(
            address = response.address.toBase58Instance(),
            chainId = response.chainId,
            decimals = response.decimals,
            coingeckoId = response.extensions.coingeckoId,
            logoUri = response.logoUri,
            name = response.name,
            symbol = response.symbol,
            tags = response.tags,
        )
    }
}
