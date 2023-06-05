package org.p2p.wallet.feerelayer.model

import java.math.BigInteger
import org.p2p.core.utils.isZero
import org.p2p.solanaj.core.FeeAmount

data class FeeInSol(
    val expectedFee: FeeAmount,
    val calculatedFee: FeeAmount
) {

    val transactionFee: BigInteger = calculatedFee.transaction
    val accountCreationFee: BigInteger = calculatedFee.accountBalances

    val isFree: Boolean = calculatedFee.total.isZero()
}
