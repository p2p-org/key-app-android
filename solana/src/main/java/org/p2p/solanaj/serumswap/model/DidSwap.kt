package org.p2p.solanaj.serumswap.model

import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

data class DidSwap(
    val givenAmount: BigInteger,
    val minExpectedSwapAmount: BigInteger,
    val fromAmount: BigInteger,
    val toAmount: BigInteger,
    val spillAmount: BigInteger,
    val fromMint: PublicKey,
    val toMint: PublicKey,
    val quoteMint: PublicKey,
    val authority: PublicKey
)
