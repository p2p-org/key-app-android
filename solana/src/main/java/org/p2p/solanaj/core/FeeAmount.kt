package org.p2p.solanaj.core

import java.math.BigInteger

data class FeeAmount(
    var transaction: BigInteger = BigInteger.ZERO,
    var accountBalances: BigInteger = BigInteger.ZERO
) {

    val total: BigInteger
        get() = transaction + accountBalances
}
