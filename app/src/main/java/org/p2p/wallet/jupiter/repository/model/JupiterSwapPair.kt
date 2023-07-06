package org.p2p.wallet.jupiter.repository.model

import java.math.BigInteger
import org.p2p.core.crypto.Base58String

data class JupiterSwapPair(
    val inputMint: Base58String,
    val outputMint: Base58String,
    val amountInLamports: BigInteger,
    val slippageBasePoints: Int
)
