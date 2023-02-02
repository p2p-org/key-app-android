package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.solanaj.utils.crypto.Base64String
import java.math.BigDecimal

data class SwapQuote(
    val inputMint: Base64String,
    val outputMint: Base64String,
    val amount: BigDecimal,
)
