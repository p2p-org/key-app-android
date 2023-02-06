package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

data class JupiterSwapToken(
    val tokenMint: Base58String,
    val chainId: Int,
    val decimals: Int,
    val coingeckoId: String?,
    val logoUri: String?,
    val tokenName: String,
    val tokenSymbol: String,
    val tags: List<String>,
    val priceInUsd: BigDecimal
)
