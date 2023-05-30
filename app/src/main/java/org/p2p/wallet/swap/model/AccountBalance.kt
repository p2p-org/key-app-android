package org.p2p.wallet.swap.model

import java.math.BigInteger
import org.p2p.solanaj.core.PublicKey

data class AccountBalance(
    val account: PublicKey,
    val amount: BigInteger,
    val decimals: Int
)
