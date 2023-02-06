package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

data class JupiterSwap(
    val inputMint: Base58String,
    val outputMint: Base58String,
    val amount: BigDecimal,
)
