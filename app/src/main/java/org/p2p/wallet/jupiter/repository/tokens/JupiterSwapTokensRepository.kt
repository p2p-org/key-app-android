package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
    suspend fun searchTokens(mintAddressOrSymbol: String): List<JupiterSwapToken>
    suspend fun searchTokensInSwappable(
        mintAddressOrSymbol: String,
        sourceTokenMint: Base58String
    ): List<JupiterSwapToken>
}
