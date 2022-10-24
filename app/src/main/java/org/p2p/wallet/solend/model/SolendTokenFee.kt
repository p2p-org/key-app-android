package org.p2p.wallet.solend.model

import org.p2p.solanaj.core.FeeAmount
import java.math.BigInteger

data class SolendTokenFee(
    val transaction: BigInteger,
    val rent: BigInteger
) {

    constructor(fee: FeeAmount) : this(fee.transaction, fee.accountBalances)

    val total: BigInteger
        get() = transaction + rent
}
