package org.p2p.wallet.pnl.models

import org.p2p.core.crypto.Base58String

data class PnlData(
    val total: PnlTokenData,
    val tokens: Map<Base58String, PnlTokenData>
) {
    fun findTokenOrDefault(tokeMints: Base58String): PnlTokenData =
        tokens[tokeMints] ?: PnlTokenData("0", "0")

    fun findToken(tokenMint: Base58String): PnlTokenData? =
        tokens[tokenMint]
}
