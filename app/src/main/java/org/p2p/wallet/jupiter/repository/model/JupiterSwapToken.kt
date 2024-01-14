package org.p2p.wallet.jupiter.repository.model

import org.p2p.core.crypto.Base58String
import org.p2p.core.token.TokenExtensions
import org.p2p.core.utils.Constants

data class JupiterSwapToken(
    val tokenMint: Base58String,
    val chainId: Int,
    val decimals: Int,
    val coingeckoId: String?,
    val logoUri: String?,
    val tokenName: String,
    val tokenSymbol: String,
    val tokenExtensions: TokenExtensions,
) {
    fun isSol(): Boolean = tokenMint.base58Value == Constants.WRAPPED_SOL_MINT

    val isStrictToken: Boolean
        get() = !tags.contains("unknown")
}

fun List<JupiterSwapToken>.findTokenByMint(mint: Base58String): JupiterSwapToken? {
    return find { it.tokenMint == mint }
}
