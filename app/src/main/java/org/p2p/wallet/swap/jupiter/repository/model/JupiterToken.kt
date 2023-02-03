package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenExtensions

data class JupiterToken(
    val address: String,
    val chainId: Int,
    val decimals: Int,
    val extensions: JupiterTokenExtensions,
    val logoURI: String,
    val name: String,
    val symbol: String,
    val tags: List<String>
)
