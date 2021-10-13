package com.p2p.wallet.swap.model

import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

data class AccountBalance(
    val account: PublicKey,
    val amount: BigInteger
)