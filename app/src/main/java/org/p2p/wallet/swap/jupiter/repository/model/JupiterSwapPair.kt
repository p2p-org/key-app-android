package org.p2p.wallet.swap.jupiter.repository.model

import java.math.BigInteger
import org.p2p.wallet.utils.Base58String

data class JupiterSwapPair(
    val inputMint: Base58String,
    val outputMint: Base58String,
    val amountInLamports: BigInteger,
    val slippageBasePoints: Int
)
