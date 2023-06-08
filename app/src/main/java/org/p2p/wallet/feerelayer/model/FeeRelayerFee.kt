package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.FeeAmount

@Parcelize
data class FeeRelayerFee(
    val transactionFeeInSol: BigInteger,
    val accountCreationFeeInSol: BigInteger,

    val transactionFeeInSpl: BigInteger,
    val accountCreationFeeInSpl: BigInteger,

    val expectedFee: FeeAmount
) : Parcelable {

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

    companion object {
        val EMPTY = FeeRelayerFee(FeeAmount(), FeeAmount(), FeeAmount())
    }
}
