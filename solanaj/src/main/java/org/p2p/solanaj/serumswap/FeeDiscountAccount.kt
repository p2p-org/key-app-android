package org.p2p.solanaj.serumswap

import org.p2p.solanaj.model.core.PublicKey
import java.math.BigInteger

data class FeeDiscountAccount(
    val balance: BigInteger,
    val mint: PublicKey,
    val pubkey: PublicKey,
    val feeTier: BigInteger
)