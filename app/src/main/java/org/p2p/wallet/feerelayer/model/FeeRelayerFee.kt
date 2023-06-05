package org.p2p.wallet.feerelayer.model

import java.math.BigInteger
import org.p2p.solanaj.core.FeeAmount

data class FeeRelayerFee(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger,

    val expectedFee: FeeAmount
) {

    constructor(feeInSol: FeeAmount, feeInSpl: FeeAmount, expectedFee: FeeAmount) : this(
        transactionFeeInSol = feeInSol.transaction,
        accountCreationFeeInSol = feeInSol.accountBalances,
        transactionFeeInSpl = feeInSpl.transaction,
        accountCreationFeeInSpl = feeInSpl.accountBalances,
        expectedFee = expectedFee
    )

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInSpl: BigInteger
        get() = transactionFeeInSpl + accountCreationFeeInSpl
}
