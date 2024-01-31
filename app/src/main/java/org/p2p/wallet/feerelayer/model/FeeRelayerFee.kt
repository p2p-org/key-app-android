package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.FeeAmount

@Parcelize
data class FeeRelayerFee constructor(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger,

    val expectedFee: FeeAmount
) : Parcelable {

    constructor(feeInSol: FeeAmount, feeInSpl: FeeAmount, expectedFee: FeeAmount) : this(
        transactionFeeInSol = feeInSol.transactionFee,
        accountCreationFeeInSol = feeInSol.accountCreationFee,
        transactionFeeInSpl = feeInSpl.transactionFee,
        accountCreationFeeInSpl = feeInSpl.accountCreationFee,
        expectedFee = expectedFee
    )

    val totalInSol: BigInteger
        get() = transactionFeeInSol + accountCreationFeeInSol

    val totalInSpl: BigInteger
        get() = transactionFeeInSpl + accountCreationFeeInSpl
}
