package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigInteger

data class JupiterSwap(
    val inputMint: Base58String,
    val outputMint: Base58String,
    val amountInLamports: BigInteger,
)
