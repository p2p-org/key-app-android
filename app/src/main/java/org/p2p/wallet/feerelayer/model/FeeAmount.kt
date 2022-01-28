package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

data class FeeAmount(
    var transaction: BigInteger,
    var accountBalances: BigInteger
) {

    val total: BigInteger
        get() = transaction + accountBalances
}